package domain

import domain.Currency.Currency

object MoneyCalculator {
  def add(amount: Double, currency: Currency, addedAmount: Double): Double =
    amount + addedAmount

  def times(amount: Double, currency: Currency, times: Int): Double =
    amount * times

  def divide(amount: Double, currency: Currency, divisor: Int): Double =
    amount / divisor
}
