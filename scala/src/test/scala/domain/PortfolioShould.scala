package domain

import domain.Currency._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class PortfolioShould extends AnyFunSuite with BeforeAndAfterEach {
  private var bank: Bank = _

  override def beforeEach(): Unit = {
    bank = Bank.withExchangeRate(EUR, USD, 1.2)
    bank.addExchangeRate(USD, KRW, 1100)
  }

  test("5 USD + 10 USD = 15 USD") {
    val portfolio = new Portfolio()
    portfolio.add(5, USD)
    portfolio.add(10, USD)

    assert(portfolio.evaluate(bank, USD) == 15)
  }

  test("5 USD + 10 EUR = 17 USD") {
    val portfolio = new Portfolio()
    portfolio.add(5, USD)
    portfolio.add(10, EUR)

    assert(portfolio.evaluate(bank, USD) == 17)
  }

  test("1 USD + 1100 KRW = 2200 KRW") {
    val portfolio = new Portfolio()
    portfolio.add(1, USD)
    portfolio.add(1100, KRW)

    assert(portfolio.evaluate(bank, KRW) == 2200)
  }

  test("5 USD + 10 EUR + 4 EUR = 21.8 USD") {
    val portfolio = new Portfolio()
    portfolio.add(5, USD)
    portfolio.add(10, EUR)
    portfolio.add(4, EUR)

    assert(portfolio.evaluate(bank, USD) == 21.8)
  }

  test(
    "Throws a MissingExchangeRatesException in case of missing exchange rates"
  ) {
    val portfolio = new Portfolio()
    portfolio.add(1, EUR)
    portfolio.add(1, USD)
    portfolio.add(1, KRW)

    val exception =
      intercept[MissingExchangeRatesException](portfolio.evaluate(bank, EUR))
    assert(
      exception.getMessage == "Missing exchange rate(s): [USD->EUR],[KRW->EUR]"
    )
  }
}
