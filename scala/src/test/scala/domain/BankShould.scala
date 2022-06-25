package domain

import domain.Currency._
import domain.DomainExtensions.MoneyExtensions
import org.scalatest.funsuite.AnyFunSuite

class BankShould extends AnyFunSuite {
  private val bank = Bank.withExchangeRate(Currency.EUR, Currency.USD, 1.2)

  test("10 EUR -> USD = 12 USD") {
    assert(bank.convert(10.euros(), USD) === 12.dollars())
  }

  test("10 EUR -> EUR = 10 EUR") {
    assert(bank.convert(10.euros(), EUR) === 10.euros())
  }

  test(
    "Throws a MissingExchangeRateException in case of missing exchange rates"
  ) {
    val exception =
      intercept[MissingExchangeRateException](bank.convert(10.euros(), KRW))
    assert(exception.getMessage == "EUR->KRW")
  }

  test("Conversion with different exchange rates EUR -> USD") {
    assert(bank.convert(10.euros(), USD) === 12.dollars())

    assert(
      bank
        .addExchangeRate(EUR, USD, 1.3)
        .convert(10.euros(), USD) === 13.dollars()
    )
  }
}
