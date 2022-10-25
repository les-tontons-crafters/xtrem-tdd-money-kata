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
            ? new Error(SameExchangeRateThanCurrency)
            : this.AddExchangeRate(exchangeRate);

    private NewBank AddExchangeRate(NewExchangeRate exchangeRate) =>
        new(this.pivotCurrency, this.exchangeRates
            .Filter(element => element.Currency != exchangeRate.Currency)
            .Add(exchangeRate));

    private bool IsPivotCurrency(Currency currency) => currency == this.pivotCurrency;

    public Either<Error, Money> Convert(Money money, Currency currency) =>
        this.GetExchangeRate(money, currency)
            .Map(rate => ConvertUsingExchangeRate(money, rate))
            .Match(Either<Error, Money>.Right,
                () => new Error(FormatMissingExchangeRate(money.Currency, currency)));

    private Option<NewExchangeRate> GetExchangeRate(Money money, Currency currency)
    {
        if (money.HasCurrency(currency))
        {
            return GetExchangeRateForSameCurrency(currency);
        }

        if (this.IsPivotCurrency(currency))
        {
            return this.FindExchangeRate(money.Currency).Bind(this.ReverseExchangeRate);
        }

        var exchangeSource = this.FindExchangeRate(money.Currency);
        var exchangeDestination = this.FindExchangeRate(currency);
        return this.ShouldConvertThroughPivotCurrency(money.Currency, currency)
            ? ComputeExchangeRate(exchangeSource, exchangeDestination)
            : exchangeDestination;
    }

    private Option<NewExchangeRate> ReverseExchangeRate(NewExchangeRate exchange) =>
        NewExchangeRate
            .From(this.pivotCurrency, exchange.GetReversedRate())
            .Match(Some, _ => Option<NewExchangeRate>.None);

    private static Option<NewExchangeRate> GetExchangeRateForSameCurrency(Currency currency) =>
        NewExchangeRate
            .From(currency, 1)
            .Match(Some, _ => Option<NewExchangeRate>.None);

    private bool ShouldConvertThroughPivotCurrency(Currency source, Currency destination) =>
        !this.IsPivotCurrency(source) && !this.IsPivotCurrency(destination);

    private static Option<NewExchangeRate> ComputeExchangeRate(Option<NewExchangeRate> source,
        Option<NewExchangeRate> destination) =>
        destination
            .Bind(exchange => source
                .Map(sourceExchange => sourceExchange.GetReversedRate())
                .Map(rate => rate * exchange.Rate)
                .Map(rate => new Tuple<Currency, double>(exchange.Currency, rate)))
            .Map(exchange => NewExchangeRate
                .From(exchange.Item1, exchange.Item2)
                .IfLeft(_ => NewExchangeRate.Default(exchange.Item1)));

    private static Money ConvertUsingExchangeRate(Money money, NewExchangeRate exchangeRate) =>
        new(money.Amount * exchangeRate.Rate, exchangeRate.Currency);

    private Option<NewExchangeRate> FindExchangeRate(Currency currency) =>
        this.exchangeRates.Find(exchange => exchange.Currency == currency);

    private static string FormatMissingExchangeRate(Currency from, Currency to) => $"{from}->{to}.";
}