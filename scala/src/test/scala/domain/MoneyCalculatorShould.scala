package domain

import domain.Currency._
import org.scalatest.funsuite.AnyFunSuite

class MoneyCalculatorShould extends AnyFunSuite {
  test("5 USD + 10 USD = 15 USD") {
    assert(
      MoneyCalculator
        .add(5, USD, 10) === 15
    )
  }

  test("10 EUR x 2 = 20 EUR") {
    assert(
      MoneyCalculator
        .times(10, EUR, 2) === 20
    )
  }

  test("4002 KRW / 4 = 1000.5 KRW") {
    assert(
      MoneyCalculator
        .divide(4002, KRW, 4) === 1000.5
    )
  }
}
