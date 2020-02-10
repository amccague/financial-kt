package uk.co.adrianlondon.financial.types

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Period

private val mathContext = MathContext.DECIMAL32

inline class MonthlyPercentageRate(val rate: BigDecimal) {

    fun asRoundedDecimal() = rate.setScale(4, RoundingMode.HALF_EVEN)!!

    fun toMonthlyPayment(term: Period, principal: BigDecimal) : MonthlyPayment {
        // pmt = P * ( i + (i / (( 1 + i )^n - 1) )
        // P = principal amount
        // i = periodic interest rate
        // n = number of periods

        val periods = term.toTotalMonths().toInt()
        val periodicGrowth = BigDecimal.ONE + rate

        val pmt = principal * (rate + (rate.divide((periodicGrowth.pow(periods) - BigDecimal.ONE), mathContext)))

        return MonthlyPayment(pmt)
    }

    fun toPresentValue(term: Period, monthlyPayment: MonthlyPayment) : BigDecimal {
        // PV = C * ( (1−(1+i)^-n)/i )
        //    = C * ( (1-(1/(1+i)^n))/i ) [avoiding the negative exponent]
        // C = periodic payment
        // i = periodic interest rate
        // n = number of periods

        val periods = term.toTotalMonths()
        val periodicGrowth = BigDecimal.ONE + rate

        return monthlyPayment.amount *
                (BigDecimal.ONE - BigDecimal.ONE.divide(periodicGrowth.pow(periods.toInt()), mathContext)).divide(rate, mathContext)
    }

    fun toAnnualRate(): AnnualPercentageRate {
        val periodicGrowth = BigDecimal.ONE + rate
        val periodsPerYear = 12

        val apr = periodicGrowth.pow(periodsPerYear) - BigDecimal.ONE

        return AnnualPercentageRate(apr)
    }
}