package money_problem.domain

import io.vavr.API.Left
import io.vavr.API.Right
import io.vavr.control.Either

class ExchangeRate private constructor(val rate: Double, val currency: Currency) {
    companion object {
        private fun isPositive(rate: Double): Boolean = rate > 0

        @JvmStatic
        fun from(rate: Double, currency: Currency): Either<Error, ExchangeRate> =
            if (isPositive(rate)) Right(ExchangeRate(rate, currency))
            else Left(Error("Exchange rate should be greater than 0"))
    }
}