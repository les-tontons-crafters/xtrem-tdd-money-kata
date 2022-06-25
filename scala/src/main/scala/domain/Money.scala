package domain

import domain.Currency.Currency

sealed case class Money(amount: Double, currency: Currency) {
  def times(times: Int): Money =
    copy(amount * times)

  def divide(divisor: Int): Money =
    copy(amount / divisor)
}
