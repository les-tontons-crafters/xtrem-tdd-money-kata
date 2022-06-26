package domain

sealed case class ConversionResult[Failure] private (
    money: Option[Money],
    failure: Option[Failure]
) {

  def this(money: Money) = this(Some(money), None)

  def this(failure: Failure) =
    this(None, Some(failure))

  def isFailure: Boolean = failure.isDefined
}

object ConversionResult {
  def fromSuccess[Failure](money: Money): ConversionResult[Failure] =
    new ConversionResult[Failure](money)

  def fromFailure[Failure](failure: Failure): ConversionResult[Failure] =
    new ConversionResult[Failure](failure)
}
