package domain

import domain.Currency.Currency

import scala.collection.mutable

sealed case class Bank private (
    private val exchangeRates: mutable.Map[String, Double] = mutable.Map()
) {
  private def keyFor(from: Currency, to: Currency): String = s"$from->$to"

  def addExchangeRate(from: Currency, to: Currency, rate: Double): Unit =
    exchangeRates(keyFor(from, to)) = rate

  def convert(amount: Double, from: Currency, to: Currency): Double = {
    if (!canConvert(from, to)) {
      throw MissingExchangeRateException(from, to)
    }
    convertSafely(amount, from, to)
  }

  private def canConvert(from: Currency, to: Currency): Boolean =
    from == to || exchangeRates.contains(keyFor(from, to))

  private def convertSafely(
      amount: Double,
      from: Currency,
      to: Currency
  ): Double =
    if (from == to) amount
    else amount * exchangeRates(keyFor(from, to))
}

object Bank {
  def withExchangeRate(from: Currency, to: Currency, rate: Double): Bank = {
    val bank = Bank()
    bank.addExchangeRate(from, to, rate)
    bank
  }
}
