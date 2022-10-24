using FsCheck;
using FsCheck.Xunit;
using LanguageExt;
using money_problem.Domain;

namespace money_problem.Tests;

public class NewBankProperties
{
    [Property]
    private Property CannotAddExchangeRateForThePivotCurrencyOfTheBank() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            GetValidRates(),
            (currency, rate) =>
                AddShouldReturnErrorForSameCurrencyAsPivot(currency, rate,
                    "Cannot add an exchange rate for the pivot currency."));

    [Property]
    private Property CanAddExchangeRateForDifferentCurrencyThanPivot() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            Arb.From<Currency>(),
            GetValidRates(),
            (pivot, currency, rate) =>
                AddShouldReturnBankForExchangeRateForDifferentCurrencyThanPivot(pivot, currency, rate)
                    .When(pivot != currency));

    private static Arbitrary<double> GetValidRates() => Arb.From<double>().MapFilter(_ => _, rate => rate > 0);

    private static bool AddShouldReturnBankForExchangeRateForDifferentCurrencyThanPivot(Currency pivot,
        Currency currency, double rate) =>
        NewBank
            .WithPivotCurrency(pivot)
            .Add(CreateExchangeRate(currency, rate))
            .IsRight;

    private static bool AddShouldReturnErrorForSameCurrencyAsPivot(Currency currency, double rate, string message) =>
        NewBank
            .WithPivotCurrency(currency)
            .Add(CreateExchangeRate(currency, rate)) == Either<Error, NewBank>.Left(new Error(message));

    private static NewExchangeRate CreateExchangeRate(Currency currency, double rate) =>
        (NewExchangeRate) NewExchangeRate.From(currency, rate).Case;
}