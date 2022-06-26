package domain

import domain.Currency._
import domain.DomainExtensions.MoneyExtensions
import org.scalatest.OptionValues
import org.scalatest.funsuite.AnyFunSuite

class BankShould extends AnyFunSuite with OptionValues {
  private val bank = Bank.withExchangeRate(Currency.EUR, Currency.USD, 1.2)

  test("10 EUR -> USD = 12 USD") {
    assert(bank.convert(10.euros(), USD).money.value === 12.dollars())
  }

  test("10 EUR -> EUR = 10 EUR") {
    assert(bank.convert(10.euros(), EUR).money.value === 10.euros())
  }

  test("Return a failure result in case of missing exchange rate") {
    assert(
      bank.convert(10.euros(), KRW).failure.value == "EUR->KRW"
    )
  }

  test("Conversion with different exchange rates EUR -> USD") {
    assert(bank.convert(10.euros(), USD).money.value === 12.dollars())

    assert(
      bank
        .addExchangeRate(EUR, USD, 1.3)
        .convert(10.euros(), USD)
        .money
        .value === 13.dollars()
    )
  }
}
