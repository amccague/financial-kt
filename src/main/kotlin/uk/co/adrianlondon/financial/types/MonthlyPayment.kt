package uk.co.adrianlondon.financial.types

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Period
import kotlin.math.abs
import kotlin.math.pow

inline class MonthlyPayment(val amount: BigDecimal) {

    constructor(amount: String) : this(BigDecimal(amount))

    fun asRoundedDecimal() = amount.setScale(2, RoundingMode.HALF_EVEN)!!

    fun toMonthlyRate(term: Period, principal: Double) : MonthlyPercentageRate? {
        // Converge on the rate by using Newton's method with each successive guess defined as:
        // nextGuess = currentGuess - ( f(currentGuess) / f'(currentGuess) )
        // Double precision is suitable when converging..

        val initialGuess = AnnualPercentageRate("0.15").toMonthlyRate() // Somewhere in the middle of our APR range.
        val precision = 1e-6

        tailrec fun findByNewtonsMethod(iterations: Int, currentGuess: Double) : MonthlyPercentageRate? {
            fun newtonsMethod(rate: Double) : Double {
                val periods = term.toTotalMonths().toInt()
                val pmt = amount.toDouble()
                val periodicGrowth = 1 + rate

                val t1 = periodicGrowth.pow(periods)
                val t2 = periodicGrowth.pow(periods-1)

                val fnDivideDerivativeFn = (principal * t1 - pmt * (t1 - 1) / rate) /
                        (principal * periods * t2 + pmt * (t1 - 1) / (rate * rate) - periods * pmt * t2 / rate)

                return rate - fnDivideDerivativeFn
            }

            val iteration = iterations + 1

            val latestGuess = newtonsMethod(currentGuess)

            val difference = abs(latestGuess - currentGuess)
            if (difference < precision) {
                return MonthlyPercentageRate(latestGuess.toBigDecimal())
            }

            if (iteration == 100) {
                return null
            }

            return findByNewtonsMethod(iteration, latestGuess)
        }

        return findByNewtonsMethod(iterations = 0, currentGuess = initialGuess.rate.toDouble())
    }
}