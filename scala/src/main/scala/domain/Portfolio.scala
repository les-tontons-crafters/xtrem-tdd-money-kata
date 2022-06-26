package domain

import domain.ConversionResult.{fromFailure, fromSuccess}
import domain.Currency.Currency

sealed class Portfolio(private val moneys: Money*) {
  def add(money: Money): Portfolio =
    new Portfolio(moneys :+ money: _*)

  def evaluate(bank: Bank, toCurrency: Currency): ConversionResult[String] = {
    val convertedMoneys = convertMoneys(bank, toCurrency)

    if (containsFailure(convertedMoneys))
      fromFailure(toFailure(convertedMoneys))
    else
      fromSuccess(sumConvertedMoney(toCurrency, convertedMoneys))
  }

  private def sumConvertedMoney(
      toCurrency: Currency,
      convertedMoneys: Seq[ConversionResult[String]]
  ): Money = {
    Money(
      convertedMoneys
        .flatMap(_.money)
        .foldLeft(0d)((acc, money) => acc + money.amount),
      toCurrency
    )
  }

  private def toFailure(
      convertedMoneys: Seq[ConversionResult[String]]
  ): String = {
    convertedMoneys
      .flatMap(_.failure)
      .map(failure => s"[$failure]")
      .mkString("Missing exchange rate(s): ", ",", "")
  }

  private def convertMoneys(
      bank: Bank,
      toCurrency: Currency
  ): Seq[ConversionResult[String]] =
    moneys.map(money => bank.convert(money, toCurrency))

  private def containsFailure(
      convertedMoneys: Seq[ConversionResult[String]]
  ): Boolean =
    convertedMoneys.exists(_.isFailure)
}
