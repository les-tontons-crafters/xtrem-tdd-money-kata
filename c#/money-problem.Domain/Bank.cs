using LanguageExt;
using static LanguageExt.Prelude;

namespace money_problem.Domain;

public class Bank
{
    public const string SameExchangeRateThanCurrency = "Cannot add an exchange rate for the pivot currency.";
    private readonly Seq<ExchangeRate> exchangeRates;
    private readonly Currency pivotCurrency;

    private Bank(Currency pivotCurrency, Seq<ExchangeRate> exchangeRates)
    {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    public static Bank WithPivotCurrency(Currency currency) => new(currency, LanguageExt.Seq<ExchangeRate>.Empty);

    public Either<Error, Bank> Add(ExchangeRate exchangeRate) =>
        this.IsPivotCurrency(exchangeRate.Currency)
            ? new Error(SameExchangeRateThanCurrency)
            : this.AddExchangeRate(exchangeRate);

    private Bank AddExchangeRate(ExchangeRate exchangeRate) =>
        new(this.pivotCurrency, this.exchangeRates
            .Filter(element => element.Currency != exchangeRate.Currency)
            .Add(exchangeRate));

    private bool IsPivotCurrency(Currency currency) => currency == this.pivotCurrency;

    public Either<Error, Money> Convert(Money money, Currency currency) =>
        this.GetExchangeRate(money, currency)
            .Map(rate => ConvertUsingExchangeRate(money, rate))
            .Match(Either<Error, Money>.Right,
                () => new Error(FormatMissingExchangeRate(money.Currency, currency)));

    private Option<ExchangeRate> GetExchangeRate(Money money, Currency currency)
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

    private Option<ExchangeRate> ReverseExchangeRate(ExchangeRate exchange) =>
        ExchangeRate
            .From(this.pivotCurrency, exchange.GetReversedRate())
            .Match(Some, _ => Option<ExchangeRate>.None);

    private static Option<ExchangeRate> GetExchangeRateForSameCurrency(Currency currency) =>
        ExchangeRate
            .From(currency, 1)
            .Match(Some, _ => Option<ExchangeRate>.None);

    private bool ShouldConvertThroughPivotCurrency(Currency source, Currency destination) =>
        !this.IsPivotCurrency(source) && !this.IsPivotCurrency(destination);

    private static Option<ExchangeRate> ComputeExchangeRate(Option<ExchangeRate> source,
        Option<ExchangeRate> destination) =>
        destination
            .Bind(exchange => source
                .Map(sourceExchange => sourceExchange.GetReversedRate())
                .Map(rate => rate * exchange.Rate)
                .Map(rate => new Tuple<Currency, double>(exchange.Currency, rate)))
            .Map(exchange => ExchangeRate
                .From(exchange.Item1, exchange.Item2)
                .IfLeft(_ => ExchangeRate.Default(exchange.Item1)));

    private static Money ConvertUsingExchangeRate(Money money, ExchangeRate exchangeRate) =>
        new(money.Amount * exchangeRate.Rate, exchangeRate.Currency);

    private Option<ExchangeRate> FindExchangeRate(Currency currency) =>
        this.exchangeRates.Find(exchange => exchange.Currency == currency);

    private static string FormatMissingExchangeRate(Currency from, Currency to) => $"{from}->{to}";
}