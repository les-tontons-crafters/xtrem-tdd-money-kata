package domain

import domain.Currency.Currency

sealed case class Bank private (
    private val exchangeRates: Map[String, Double] = Map.empty
) {
  private def keyFor(from: Currency, to: Currency): String = s"$from->$to"

  def addExchangeRate(from: Currency, to: Currency, rate: Double): Bank =
    Bank(exchangeRates.updated(keyFor(from, to), rate))

  def convert(money: Money, toCurrency: Currency): Either[String, Money] = {
    if (canConvert(money, toCurrency))
      Right(convertSafely(money, toCurrency))
    else Left(s"${money.currency}->$toCurrency")
  }

  private def canConvert(money: Money, toCurrency: Currency): Boolean =
    money.currency == toCurrency || exchangeRates.contains(
      keyFor(money.currency, toCurrency)
    )

  private def convertSafely(money: Money, toCurrency: Currency): Money =
    if (money.currency == toCurrency) money
    else
      Money(
        money.amount * exchangeRates(keyFor(money.currency, toCurrency)),
        toCurrency
      )
}

object Bank {
  def withExchangeRate(from: Currency, to: Currency, rate: Double): Bank =
    Bank().addExchangeRate(from, to, rate)
}
