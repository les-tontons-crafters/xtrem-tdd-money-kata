package domain

import domain.Currency._

object DomainExtensions {
  implicit class MoneyExtensions(val amount: Double) {
    def euros(): Money = Money(amount, EUR)
    def dollars(): Money = Money(amount, USD)
    def koreanWons(): Money = Money(amount, KRW)
  }
}
