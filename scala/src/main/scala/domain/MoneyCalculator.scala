package domain

import domain.Currency.Currency

object MoneyCalculator {
  def times(amount: Double, currency: Currency, times: Int): Double =
    amount * times

  def divide(amount: Double, currency: Currency, divisor: Int): Double =
    amount / divisor
}
