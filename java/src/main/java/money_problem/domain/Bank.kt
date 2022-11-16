package money_problem.domain

import io.vavr.control.Either
import io.vavr.control.Either.left
import io.vavr.control.Either.right

typealias CanConvert = (Money, Currency) -> Boolean
typealias Convert = (Money, Currency) -> Money

class Bank private constructor(
    private val pivotCurrency: Currency,
    private val exchangeRates: Map<String, ExchangeRate> = emptyMap()
) {
    private val convert: Map<CanConvert, Convert> = mapOf(
        { money: Money, to: Currency -> isSameCurrency(money.currency, to) } to { money: Money, _: Currency -> money },
        { money: Money, to: Currency -> canConvertDirectly(money, to) } to { money: Money, to: Currency -> convertDirectly(money, to) },
        { money: Money, to: Currency -> canConvertThroughPivotCurrency(money, to) } to { money: Money, to: Currency -> convertThroughPivotCurrency(money, to) }
    )

    fun add(exchangeRate: ExchangeRate): Either<Error, Bank> =
        if (!isSameCurrency(exchangeRate.currency, pivotCurrency))
            right(addMultiplierAndDividerExchangeRate(exchangeRate))
        else left(Error("Can not add an exchange rate for the pivot currency"))

    private fun isSameCurrency(currency: Currency, otherCurrency: Currency): Boolean = currency == otherCurrency

    private fun addMultiplierAndDividerExchangeRate(exchangeRate: ExchangeRate): Bank =
        Bank(
            pivotCurrency, exchangeRates
                    + (keyFor(pivotCurrency, exchangeRate.currency) to exchangeRate)
                    + (keyFor(exchangeRate.currency, pivotCurrency) to dividerRate(exchangeRate))
        )

    private fun dividerRate(exchangeRate: ExchangeRate): ExchangeRate =
        ExchangeRate.from(1 / exchangeRate.rate, exchangeRate.currency).orNull

    fun convert(money: Money, to: Currency): Either<Error, Money> =
        convert.filterKeys { canConvert -> canConvert(money, to) }
            .firstNotNullOfOrNull { convert -> convert.value(money, to) }
            .toEither(money.currency, to)

    private fun canConvertDirectly(money: Money, to: Currency): Boolean =
        exchangeRates.containsKey(keyFor(money.currency, to))

    private fun canConvertThroughPivotCurrency(money: Money, to: Currency): Boolean =
        (exchangeRates.containsKey(keyFor(pivotCurrency, money.currency))
                && exchangeRates.containsKey(keyFor(pivotCurrency, to)))

    private fun convertDirectly(money: Money, to: Currency): Money =
        exchangeRates[keyFor(money.currency, to)]
            ?.let { Money(money.amount * it.rate, to) }!!

    private fun convertThroughPivotCurrency(money: Money, to: Currency): Money =
        convertDirectly(convertDirectly(money, pivotCurrency), to)

    companion object {
        @JvmStatic
        fun withPivotCurrency(pivotCurrency: Currency): Bank = Bank(pivotCurrency)
        private fun keyFor(from: Currency, to: Currency): String = "$from->$to"

        private fun Money?.toEither(from: Currency, to: Currency): Either<Error, Money> =
            if (this != null) right(this) else left(Error(keyFor(from, to)))
    }
}