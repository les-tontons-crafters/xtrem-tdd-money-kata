using System.Collections.Immutable;

namespace money_problem.Domain;

public class Portfolio
{
    private readonly ICollection<Money> moneys;

    public Portfolio()
    {
        this.moneys = new List<Money>();
    }

    private Portfolio(IEnumerable<Money> moneys)
    {
        this.moneys = moneys.ToImmutableList();
    }

    public Money Evaluate(Bank bank, Currency currency)
    {
        List<ConversionResult> results = this.GetConvertedMoneys(bank, currency);
        return ContainsFailure(results)
            ? throw ToException(results)
            : ToMoney(results, currency);
    }

    private static MissingExchangeRatesException ToException(IEnumerable<ConversionResult> results) =>
        new(results
            .Where(result => result.HasException())
            .Select(result => result.GetExceptionUnsafe())
            .ToList());

    private static Money ToMoney(IEnumerable<ConversionResult> results, Currency currency) =>
        new(results.Sum(result => result.GetMoneyUnsafe().Amount), currency);

    private static bool ContainsFailure(IEnumerable<ConversionResult> results) =>
        results.Any(result => result.HasException());

    private List<ConversionResult> GetConvertedMoneys(Bank bank, Currency currency) =>
        this.moneys
            .Select(money => ConvertMoney(bank, currency, money))
            .ToList();

    private static ConversionResult ConvertMoney(Bank bank, Currency currency, Money money)
    {
        try
        {
            return new ConversionResult(bank.Convert(money, currency));
        }
        catch (MissingExchangeRateException exception)
        {
            return new ConversionResult(exception);
        }
    }

    public Portfolio Add(Money money)
    {
        List<Money> updatedMoneys = this.moneys.ToList();
        updatedMoneys.Add(money);
        return new Portfolio(updatedMoneys);
    }

    private class ConversionResult
    {
        private readonly MissingExchangeRateException? exception;

        private readonly Money? money;

        public ConversionResult(Money money)
        {
            this.money = money;
        }

        public ConversionResult(MissingExchangeRateException exception)
        {
            this.exception = exception;
        }

        public bool HasMoney() => this.money != null;

        public bool HasException() => this.exception != null;

        public MissingExchangeRateException GetExceptionUnsafe() => this.exception!;

        public Money GetMoneyUnsafe() => this.money!;
    }
}