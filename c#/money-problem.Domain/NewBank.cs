using LanguageExt;
using static LanguageExt.Prelude;

namespace money_problem.Domain;

public class NewBank
{
    public const string SameExchangeRateThanCurrency = "Cannot add an exchange rate for the pivot currency.";
    private readonly Seq<NewExchangeRate> exchangeRates;
    private readonly Currency pivotCurrency;

    private NewBank(Currency pivotCurrency, Seq<NewExchangeRate> exchangeRates)
    {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    public static NewBank WithPivotCurrency(Currency currency) => new(currency, LanguageExt.Seq<NewExchangeRate>.Empty);

    public Either<Error, NewBank> Add(NewExchangeRate exchangeRate) =>
        this.IsPivotCurrency(exchangeRate.Currency)
            ? Either<Error, NewBank>.Left(new Error(SameExchangeRateThanCurrency))
            : Either<Error, NewBank>.Right(this.AddExchangeRate(exchangeRate));

    private NewBank AddExchangeRate(NewExchangeRate exchangeRate) =>
        new(this.pivotCurrency, this.exchangeRates
            .Filter(element => element.Currency != exchangeRate.Currency)
            .Add(exchangeRate));

    private bool IsPivotCurrency(Currency currency) => currency == this.pivotCurrency;

    public Either<Error, Money> Convert(Money money, Currency currency) =>
        this.GetExchangeRate(money, currency)
            .Map(rate => ConvertUsingExchangeRate(money, rate))
            .Match(some => Either<Error, Money>.Right(some),
                () => Either<Error, Money>.Left(new Error(FormatMissingExchangeRate(money.Currency, currency))));

    private Option<NewExchangeRate> GetExchangeRate(Money money, Currency currency) =>
        money.HasCurrency(currency)
            ? NewExchangeRate
                .From(currency, 1)
                .Match(Some, _ => Option<NewExchangeRate>.None)
            : this.FindExchangeRate(currency);

    private static Money ConvertUsingExchangeRate(Money money, NewExchangeRate exchangeRate) =>
        new(money.Amount * exchangeRate.Rate, exchangeRate.Currency);

    private Option<NewExchangeRate> FindExchangeRate(Currency currency) =>
        this.exchangeRates.Find(exchange => exchange.Currency == currency);

    private static string FormatMissingExchangeRate(Currency from, Currency to) => $"{from}->{to}.";
}