package domain

import domain.Currency.Currency

class Portfolio() {
  private var moneys: List[Money] = List.empty

  def add(money: Money): Unit =
    moneys = moneys :+ money

  def evaluate(bank: Bank, toCurrency: Currency): Money = {
    var convertedResult = 0d
    var missingExchangeRates: Seq[MissingExchangeRateException] = Seq.empty

    for (money <- moneys) {
      try {
        val convertedAmount = bank.convert(money, toCurrency)
        convertedResult += convertedAmount.amount
      } catch {
        case missingExchangeRate: MissingExchangeRateException =>
          missingExchangeRates = missingExchangeRates :+ missingExchangeRate
      }
    }

    if (missingExchangeRates.nonEmpty)
      throw MissingExchangeRatesException(missingExchangeRates.toSeq)

    Money(convertedResult, toCurrency)
  }
}
