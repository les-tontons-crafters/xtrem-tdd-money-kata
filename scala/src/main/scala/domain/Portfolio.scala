package domain

import domain.Currency.Currency

class Portfolio() {
  private var moneys: List[Money] = List.empty

  def add(money: Money): Unit =
    moneys = moneys :+ money

  private def convertMoney(
      bank: Bank,
      money: Money,
      toCurrency: Currency
  ): ConversionResult = {
    try {
      new ConversionResult(bank.convert(money, toCurrency))
    } catch {
      case missingExchangeRate: MissingExchangeRateException =>
        new ConversionResult(missingExchangeRate)
    }
  }

  def evaluate(bank: Bank, toCurrency: Currency): Money = {
    val convertedMoneys = convertMoneys(bank, toCurrency)

    if (containsFailure(convertedMoneys))
      throw toMissingExchangeRatesException(convertedMoneys)
    else toMoney(toCurrency, convertedMoneys)
  }

  private def convertMoneys(
      bank: Bank,
      toCurrency: Currency
  ): Seq[ConversionResult] =
    moneys.map(money => convertMoney(bank, money, toCurrency))

  private def containsFailure(convertedMoneys: Seq[ConversionResult]): Boolean =
    convertedMoneys.exists(_.isFailure)

  private def toMissingExchangeRatesException(
      convertedMoneys: Seq[ConversionResult]
  ) =
    MissingExchangeRatesException(
      convertedMoneys
        .flatMap(_.missingExchangeRate)
    )

  private def toMoney(
      toCurrency: Currency,
      convertedMoneys: Seq[ConversionResult]
  ): Money =
    Money(
      convertedMoneys
        .flatMap(_.money)
        .foldLeft(0d)((acc, money) => acc + money.amount),
      toCurrency
    )
}
