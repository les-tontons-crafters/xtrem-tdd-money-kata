package domain

import domain.Currency._
import org.scalatest.funsuite.AnyFunSuite

class BankShould extends AnyFunSuite {
  private val bank = Bank.withExchangeRate(Currency.EUR, Currency.USD, 1.2)

  test("10 EUR -> USD = 12 USD") {
    assert(bank.convert(10, EUR, USD) === 12)
  }

  test("10 EUR -> EUR = 10 EUR") {
    assert(bank.convert(10, EUR, EUR) === 10)
  }

  test(
    "Throws a MissingExchangeRateException in case of missing exchange rates"
  ) {
    assertThrows[MissingExchangeRateException](bank.convert(10, EUR, KRW))
  }

  test("Conversion with different exchange rates EUR -> USD") {
    assert(bank.convert(10, EUR, USD) === 12)
    bank.addExchangeRate(EUR, USD, 1.3)

    assert(bank.convert(10, EUR, USD) === 13)
  }
}
