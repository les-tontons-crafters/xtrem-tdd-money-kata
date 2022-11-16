package money_problem.domain

import io.vavr.control.Either
import io.vavr.control.Either.left
import io.vavr.control.Either.right

class Portfolio {
    private val moneys: List<Money>

    constructor() {
        moneys = emptyList()
    }

    private constructor(moneys: List<Money>) {
        this.moneys = moneys
    }

    fun add(money: Money): Portfolio = Portfolio(moneys + money)

    fun evaluate(bank: Bank, to: Currency): Either<Error, Money> =
        convertAllMoneys(bank, to)
            .let {
                if (containsFailure(it)) left(toFailure(it))
                else right(sumConvertedMoney(it, to))
            }

    private fun convertAllMoneys(bank: Bank, toCurrency: Currency): List<Either<Error, Money>> =
        moneys.map { money: Money -> bank.convert(money, toCurrency) }

    private fun containsFailure(convertedMoneys: List<Either<Error, Money>>): Boolean =
        convertedMoneys.any { result -> result.isLeft }

    private fun toFailure(convertedMoneys: List<Either<Error, Money>>): Error =
        Error(convertedMoneys
            .filter { result -> result.isLeft }
            .joinToString(",", "Missing exchange rate(s): ") { result -> String.format("[%s]", result.left.message) })

    private fun sumConvertedMoney(convertedMoneys: List<Either<Error, Money>>, toCurrency: Currency): Money =
        Money(
            convertedMoneys
                .filter { result -> result.isRight }
                .map { result -> result.getOrElse(Money(0.0, toCurrency)) }
                .sumOf { result -> result.amount },
            toCurrency
        )
}