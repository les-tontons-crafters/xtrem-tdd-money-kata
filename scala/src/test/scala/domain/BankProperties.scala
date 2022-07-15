package domain

import domain.BankProperties.{bank, currencyGenerator, roundTripConvert}
import domain.Currency._
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic.Tolerance.convertNumericToPlusOrMinusWrapper
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.Checkers

class BankProperties extends AnyFunSuite with Checkers with EitherValues {
  test("convert in same currency should return original Money") {
    forAll { (originalAmount: Double, currency: Currency) =>
      {
        val originalMoney = Money(originalAmount, currency)
        bank.convert(originalMoney, currency).value == originalMoney
      }
    }.check()
  }

  test("round tripping in different currencies") {
    forAll { (originalAmount: Double, from: Currency, to: Currency) =>
      {
        roundTripConvert(originalAmount, from, to).value == Money(
          originalAmount,
          from
        )
      }
    }.check()
  }

  test("round trip in error") {
    val originalAmount = -2.1645893211081448e69
    val roundTripAmount = roundTripConvert(originalAmount, EUR, USD).value

    assert(roundTripAmount.amount === originalAmount +- 0.01)
  }

  private def toleranceFor(originalAmount: Double): Double =
    Math.abs(0.01 * originalAmount)
}

object BankProperties {
  private val exchangeRates: Map[(Currency, Currency), Double] = Map(
    (EUR, USD) -> 1.2,
    (USD, EUR) -> 0.82,
    (USD, KRW) -> 1100d,
    (KRW, USD) -> 0.0009,
    (EUR, KRW) -> 1344d,
    (KRW, EUR) -> 0.00073
  )

  private val bank: Bank = createBank()

  private def newBank(): Bank = {
    val firstEntry = exchangeRates.head
    Bank.withExchangeRate(firstEntry._1._1, firstEntry._1._2, firstEntry._2)
  }

  private def createBank(): Bank =
    exchangeRates
      .drop(1)
      .foldLeft(newBank()) {
        case (bank, ((from, to), rate)) => bank.addExchangeRate(from, to, rate)
      }

  private def roundTripConvert(
      originalAmount: Double,
      from: Currency,
      to: Currency
  ): Either[String, Money] = {
    bank
      .convert(Money(originalAmount, from), to)
      .flatMap(convertedMoney => bank.convert(convertedMoney, from))
  }

  implicit def currencyGenerator: Arbitrary[Currency] =
    Arbitrary {
      Gen.oneOf(Currency.values.toSeq)
    }
}
