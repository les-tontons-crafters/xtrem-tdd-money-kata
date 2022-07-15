package domain

import domain.BankProperties.{
  amountsAreClosed,
  bank,
  currencyGenerator,
  moneyGenerator,
  roundTripConvert
}
import domain.Currency._
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.Checkers

import java.lang.Math._

class BankProperties extends AnyFunSuite with Checkers with EitherValues {
  test("convert in same currency should return original Money") {
    check(forAll { (originalMoney: Money) =>
      bank
        .convert(originalMoney, originalMoney.currency)
        .value == originalMoney
    })
  }

  test("round tripping in different currencies") {
    check(forAll { (originalMoney: Money, to: Currency) =>
      {
        amountsAreClosed(
          originalMoney,
          roundTripConvert(originalMoney, to).value
        )
      }
    })
  }
}

object BankProperties {
  private val exchangeRates: Map[(Currency, Currency), Double] = Map(
    (EUR, USD) -> 1.0567,
    (USD, EUR) -> 0.9466,
    (USD, KRW) -> 1302.0811,
    (KRW, USD) -> 0.00076801737,
    (EUR, KRW) -> 1368.51779,
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
      originalMoney: Money,
      to: Currency
  ): Either[String, Money] =
    bank
      .convert(originalMoney, to)
      .flatMap(convertedMoney =>
        bank.convert(convertedMoney, originalMoney.currency)
      )

  private def amountsAreClosed(
      originalMoney: Money,
      roundTripMoney: Money
  ): Boolean =
    abs(roundTripMoney.amount - originalMoney.amount) <=
      toleranceFor(originalMoney)

  private def toleranceFor(money: Money): Double =
    abs(0.01 * money.amount)

  implicit def currencyGenerator: Arbitrary[Currency] =
    Arbitrary {
      Gen.oneOf(Currency.values.toSeq)
    }

  private val maxAmount = 1_000_000_000

  implicit def moneyGenerator: Arbitrary[Money] =
    Arbitrary {
      for {
        amount <- Gen.choose(-maxAmount, maxAmount)
        currency <- Arbitrary.arbitrary[Currency]
      } yield Money(amount, currency)
    }
}
