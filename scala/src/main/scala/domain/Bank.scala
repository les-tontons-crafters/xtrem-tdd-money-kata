package domain

import domain.Currency.Currency

import scala.collection.mutable

sealed case class Bank private (
    private val exchangeRates: mutable.Map[String, Double] = mutable.Map()
) {
  private def keyFor(from: Currency, to: Currency): String = s"$from->$to"

  def addExchangeRate(from: Currency, to: Currency, rate: Double): Unit =
    exchangeRates(keyFor(from, to)) = rate

  def convert(money: Money, toCurrency: Currency): Money = {
    if (!canConvert(money, toCurrency)) {
      throw MissingExchangeRateException(money.currency, toCurrency)
    }
    convertSafely(money, toCurrency)
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
  def withExchangeRate(from: Currency, to: Currency, rate: Double): Bank = {
    val bank = Bank()
    bank.addExchangeRate(from, to, rate)
    bank
  }
}
