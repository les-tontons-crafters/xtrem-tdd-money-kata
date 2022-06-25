package domain

sealed case class ConversionResult private (
    money: Option[Money],
    missingExchangeRate: Option[MissingExchangeRateException]
) {
  def this(money: Money) = this(Some(money), None)

  def this(missingExchangeRate: MissingExchangeRateException) =
    this(None, Some(missingExchangeRate))

  def isFailure: Boolean = missingExchangeRate.isDefined
}
