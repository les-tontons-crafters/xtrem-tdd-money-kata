package domain

import domain.DomainExtensions.MoneyExtensions
import org.scalatest.funsuite.AnyFunSuite

class MoneyShould extends AnyFunSuite {
  test("10 EUR x 2 = 20 EUR") {
    assert(
      10.euros()
        .times(2) === 20.euros()
    )
  }

  test("4002 KRW / 4 = 1000.5 KRW") {
    assert(
      4002
        .koreanWons()
        .divide(4) === 1000.5.koreanWons()
    )
  }
}
