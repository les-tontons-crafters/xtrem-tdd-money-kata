package domain

import domain.Currency.Currency

sealed class Portfolio(private val moneys: Money*) {
  def add(money: Money): Portfolio =
    new Portfolio(moneys :+ money: _*)

  def evaluate(bank: Bank, toCurrency: Currency): Either[String, Money] = {
    val convertedMoneys = convertMoneys(bank, toCurrency)

    if (containsFailure(convertedMoneys))
      Left(toFailure(convertedMoneys))
    else Right(sumConvertedMoney(toCurrency, convertedMoneys))
  }

  private def convertMoneys(
      bank: Bank,
      toCurrency: Currency
  ): Seq[Either[String, Money]] =
    moneys.map(money => bank.convert(money, toCurrency))

  private def sumConvertedMoney(
      toCurrency: Currency,
      convertedMoneys: Seq[Either[String, Money]]
  ): Money = {
    Money(
      convertedMoneys
        .collect { case Right(x) => x }
        .foldLeft(0d)((acc, money) => acc + money.amount),
      toCurrency
    )
  }

  private def containsFailure(
      convertedMoneys: Seq[Either[String, Money]]
  ): Boolean = convertedMoneys.exists(_.isLeft)

  private def toFailure(
      convertedMoneys: Seq[Either[String, Money]]
  ): String = {
    convertedMoneys
      .collect { case Left(x) => x }
      .map(failure => s"[$failure]")
      .mkString("Missing exchange rate(s): ", ",", "")
  }
}
