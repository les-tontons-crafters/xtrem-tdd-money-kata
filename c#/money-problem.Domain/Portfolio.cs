namespace money_problem.Domain;

public class Portfolio
{
    private readonly ICollection<Money> moneys = new List<Money>();

    public void Add(Money money) => this.moneys.Add(money);

    public Money Evaluate(Bank bank, Currency currency)
    {
        List<ConversionResult> results = this.moneys
            .Select(money => ConvertMoney(bank, currency, money))
            .ToList();
        return results.Any(result => result.HasException())
            ? throw new MissingExchangeRatesException(results
                .Where(result => result.HasException())
                .Select(result => result.GetExceptionUnsafe())
                .ToList())
            : new Money(results.Sum(result => result.GetMoneyUnsafe().Amount), currency);
    }

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