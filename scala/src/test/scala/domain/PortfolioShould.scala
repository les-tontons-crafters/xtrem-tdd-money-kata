package domain

import domain.Currency._
import domain.DomainExtensions.MoneyExtensions
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues}

class PortfolioShould
    extends AnyFunSuite
    with BeforeAndAfterEach
    with OptionValues
    with EitherValues {
  private var bank: Bank = _

  override def beforeEach(): Unit = {
    bank = Bank
      .withExchangeRate(EUR, USD, 1.2)
      .addExchangeRate(USD, KRW, 1100)
  }

  test("5 USD + 10 USD = 15 USD") {
    assert(
      portfolioWith(
        5.dollars(),
        10.dollars()
      ).evaluate(bank, USD).value == 15.dollars()
    )
  }

  test("5 USD + 10 EUR = 17 USD") {
    assert(
      portfolioWith(
        5.dollars(),
        10.euros()
      ).evaluate(bank, USD).value == 17.dollars()
    )
  }

  test("1 USD + 1100 KRW = 2200 KRW") {
    assert(
      portfolioWith(
        1.dollars(),
        1100.koreanWons()
      ).evaluate(bank, KRW).value == 2200.koreanWons()
    )
  }

  test("5 USD + 10 EUR + 4 EUR = 21.8 USD") {
    assert(
      portfolioWith(
        5.dollars(),
        10.euros(),
        4.euros()
      ).evaluate(bank, USD).value == 21.8.dollars()
    )
  }

  test(
    "Return a failure in case of missing exchange rates"
  ) {
    assert(
      portfolioWith(
        1.euros(),
        1.dollars(),
        1.koreanWons()
      ).evaluate(bank, EUR)
        .left
        .value == "Missing exchange rate(s): [USD->EUR],[KRW->EUR]"
    )
  }

  private def portfolioWith(moneys: Money*): Portfolio =
    moneys.foldLeft(new Portfolio())((portfolio, money) => portfolio.add(money))
}
