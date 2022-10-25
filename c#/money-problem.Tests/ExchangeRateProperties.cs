using FsCheck;
using FsCheck.Xunit;
using LanguageExt;
using money_problem.Domain;

namespace money_problem.Tests;

public class ExchangeRateProperties
{
    [Property]
    public Property CannotUseNegativeDoubleOrZeroAsExchangeRate() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            GetInvalidRates(),
            (currency, rate) =>
                ExchangeRateShouldReturnError(currency, rate, "Exchange rate should be greater than 0."));

    [Property]
    public Property CanUsePositiveAsExchangeRate() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            GetValidRates(),
            ExchangeRateShouldReturnRate);

    private static Arbitrary<double> GetValidRates() => Arb.From<double>().MapFilter(_ => _, rate => rate > 0);

    private static Arbitrary<double> GetInvalidRates() => Arb.From<double>().MapFilter(_ => _, rate => rate <= 0);

    private static bool ExchangeRateShouldReturnRate(Currency currency, double rate) =>
        ExchangeRate.From(currency, rate)
            .Map(value => value.Currency == currency && value.Rate == rate)
            .IfLeft(false);

    private static bool ExchangeRateShouldReturnError(Currency currency, double rate, string message) =>
        ExchangeRate.From(currency, rate) ==
        Either<Error, ExchangeRate>.Left(new Error(message));
}