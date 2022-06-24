package domain

sealed case class MissingExchangeRatesException(
    missingExchangeRates: Seq[MissingExchangeRateException]
) extends Exception(
      missingExchangeRates
        .map(e => s"[${e.getMessage}]")
        .mkString("Missing exchange rate(s): ", ",", "")
    ) {}
