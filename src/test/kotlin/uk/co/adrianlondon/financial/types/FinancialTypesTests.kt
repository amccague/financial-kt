package uk.co.adrianlondon.financial.types

import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec
import io.kotlintest.tables.forAll
import io.kotlintest.tables.headers
import io.kotlintest.tables.row
import io.kotlintest.tables.table
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Period

class AnnualPercentageRateTests: ShouldSpec({

    should("convert to monthly rate and back again") {
        assertAll(GenPercentageRate()) {
            AnnualPercentageRate(it).toMonthlyRate().toAnnualRate().asRoundedDecimal() shouldBe it
        }
    }

})

class MonthlyPercentageRateTests: ShouldSpec({

    should("calculate monthly periodic payment") {
        table(
            headers("apr", "amount", "term", "expected monthly repayment"),
            row(AnnualPercentageRate("0.03"), 10_000, Period.ofMonths(24), MonthlyPayment("429.63")),
            row(AnnualPercentageRate("0.045"), 5_000, Period.ofMonths(60), MonthlyPayment("93.01")),
            row(AnnualPercentageRate("0.104"), 1_000, Period.ofMonths(12), MonthlyPayment("87.89"))
        ).forAll { apr, amount, term, expectedMonthlyRepayment ->
            val principal = amount.toBigDecimal()
            val monthlyPercentageRate = apr.toMonthlyRate()
            val monthlyRepayment = monthlyPercentageRate.toMonthlyPayment(term, principal)

            monthlyRepayment.asRoundedDecimal() shouldBe expectedMonthlyRepayment.asRoundedDecimal()
        }
    }

    should("convert to annual rate and back again") {
        assertAll(GenPercentageRate()) {
            MonthlyPercentageRate(it).toAnnualRate().toMonthlyRate().asRoundedDecimal() shouldBe it
        }
    }

    should("calculate a present value") {
        table(
            headers("apr", "term", "monthly payment", "expected present value"),
            row(AnnualPercentageRate("0.104"), Period.ofMonths(12), MonthlyPayment("87.886"), 1_000),
            row(AnnualPercentageRate("0.045"), Period.ofMonths(60), MonthlyPayment("93.01"), 5_000),
            row(AnnualPercentageRate("0.03"), Period.ofMonths(24), MonthlyPayment("429.633"), 10_000)
        ).forAll { apr, term, monthlyPayment, expectedPresentValue ->
            val monthlyPercentageRate = apr.toMonthlyRate()
            val presentValue = monthlyPercentageRate.toPresentValue(term, monthlyPayment)

            presentValue.setScale(2, RoundingMode.HALF_EVEN) shouldBe expectedPresentValue.toBigDecimal().setScale(2)
        }
    }

})

class MonthlyPaymentTests: ShouldSpec({

    should("find the rate from a monthly payment with term and principal") {
        table(
            headers("monthly payment", "term", "principal", "expected APR"),
            row(MonthlyPayment("87.886"), Period.ofMonths(12), 1_000, AnnualPercentageRate("0.104")),
            row(MonthlyPayment("93.01"), Period.ofMonths(60), 5_000, AnnualPercentageRate("0.045")),
            row(MonthlyPayment("429.63"), Period.ofMonths(24), 10_000, AnnualPercentageRate("0.03"))
        ).forAll { monthlyPayment, term, principal, expectedApr ->
            val apr = monthlyPayment.toMonthlyRate(term, principal.toDouble())!!.toAnnualRate()

            apr.asRoundedDecimal() shouldBe expectedApr.asRoundedDecimal()
        }
    }

})

class GenPercentageRate : Gen<BigDecimal> {
    override fun constants(): Iterable<BigDecimal> =
        listOf(
            BigDecimal("0.0100"),
            BigDecimal("0.3500"))

    override fun random(): Sequence<BigDecimal> =
        generateSequence {
            (50..3500).random().toBigDecimal() * BigDecimal("0.0100")
        }
}