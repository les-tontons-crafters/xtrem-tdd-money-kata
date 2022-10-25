using FsCheck;
using FsCheck.Xunit;
using LanguageExt;
using money_problem.Domain;

namespace money_problem.Tests;

public class BankProperties
{
    [Property]
    private Property CannotAddExchangeRateForThePivotCurrencyOfTheBank() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            GetValidRates(),
            (currency, rate) =>
                AddShouldReturnErrorForSameCurrencyAsPivot(currency, rate,
                    Bank.SameExchangeRateThanCurrency));

    [Property]
    private Property CanAddExchangeRateForDifferentCurrencyThanPivot() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            Arb.From<Currency>(),
            GetValidRates(),
            (pivot, currency, rate) =>
                AddShouldReturnBankForExchangeRateForDifferentCurrencyThanPivot(pivot, currency, rate)
                    .When(pivot != currency));

    [Property]
    private Property CanUpdateExchangeRateForAnyCurrencyDifferentThanPivot() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            Arb.From<Currency>(),
            GetValidRates(),
            (pivot, currency, rate) => AddShouldReturnBankWhenUpdatingExchangeRate(pivot, currency, rate)
                .When(pivot != currency));

    [Property]
    private Property CannotConvertToUnknownCurrency() =>
        Prop.ForAll(Arb.From<Currency>(),
            Arb.From<Currency>(),
            MoneyGenerator.GenerateMoneys(),
            (pivot, currency, money) => ConvertShouldReturnErrorWhenCurrencyIsUnknown(pivot, money, currency)
                .When(pivot != currency && money.Currency != currency));

    [Property]
    private Property ConvertToSameCurrencyReturnSameMoney() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            MoneyGenerator.GenerateMoneys(),
            (pivot, money) => ConvertShouldReturnMoneyWhenConvertingToSameCurrency(pivot, money)
                .When(pivot != money.Currency));

    private static bool ConvertShouldReturnMoneyWhenConvertingToSameCurrency(Currency pivot, Money money) =>
        Bank.WithPivotCurrency(pivot).Convert(money, money.Currency) == money;

    private static bool ConvertShouldReturnErrorWhenCurrencyIsUnknown(Currency pivot, Money money, Currency currency) =>
        Bank.WithPivotCurrency(pivot).Convert(money, currency) ==
        Either<Error, Money>.Left(new Error($"{money.Currency}->{currency}"));

    private static bool AddShouldReturnBankWhenUpdatingExchangeRate(Currency pivot, Currency currency, double rate) =>
        Bank.WithPivotCurrency(pivot)
            .Add(DomainUtility.CreateExchangeRate(currency, rate))
            .Map(bank => bank.Add(DomainUtility.CreateExchangeRate(currency, rate + 1)))
            .IsRight;

    private static Arbitrary<double> GetValidRates() => Arb.From<double>().MapFilter(_ => _, rate => rate > 0);

    private static bool AddShouldReturnBankForExchangeRateForDifferentCurrencyThanPivot(Currency pivot,
        Currency currency, double rate) =>
        Bank
            .WithPivotCurrency(pivot)
            .Add(DomainUtility.CreateExchangeRate(currency, rate))
            .IsRight;

    private static bool AddShouldReturnErrorForSameCurrencyAsPivot(Currency currency, double rate, string message) =>
        Bank
            .WithPivotCurrency(currency)
            .Add(DomainUtility.CreateExchangeRate(currency, rate)) == Either<Error, Bank>.Left(new Error(message));
}