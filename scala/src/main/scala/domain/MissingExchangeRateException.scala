package domain

import domain.Currency.Currency

sealed case class MissingExchangeRateException(
    private val from: Currency,
    private val to: Currency
) extends Exception(s"$from->$to") {}
