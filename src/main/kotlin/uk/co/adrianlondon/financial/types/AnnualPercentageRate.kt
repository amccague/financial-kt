package uk.co.adrianlondon.financial.types

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

inline class AnnualPercentageRate(private val rate: BigDecimal) {

    constructor(rate: String) : this(BigDecimal(rate))

    fun asRoundedDecimal() = rate.setScale(4, RoundingMode.HALF_EVEN)!!

    fun toMonthlyRate(): MonthlyPercentageRate {
        val periodicGrowth = (BigDecimal.ONE + rate).toDouble()
        val periodsPerMonth = 1 / 12.0

        val mpr = periodicGrowth.pow(periodsPerMonth).toBigDecimal() - BigDecimal.ONE

        return MonthlyPercentageRate(mpr)
    }
}
