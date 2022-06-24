package domain

import domain.Currency.Currency

import scala.collection.mutable

class Portfolio() {
  private val moneys: mutable.Map[Currency, List[Double]] =
    mutable.Map.empty.withDefault(_ => List.empty)

  def add(amount: Double, currency: Currency): Unit =
    moneys(currency) = moneys(currency) :+ amount

  def evaluate(bank: Bank, toCurrency: Currency): Double = {
    var convertedResult = 0d
    var missingExchangeRates: Seq[MissingExchangeRateException] = Seq.empty

    for (money <- moneys) {
      for (amount <- money._2) {
        try {
          val convertedAmount = bank.convert(amount, money._1, toCurrency)
          convertedResult += convertedAmount
        } catch {
          case missingExchangeRate: MissingExchangeRateException =>
            missingExchangeRates = missingExchangeRates :+ missingExchangeRate
        }
      }
    }

    if (missingExchangeRates.nonEmpty)
      throw MissingExchangeRatesException(missingExchangeRates.toSeq)
    convertedResult
  }
}
