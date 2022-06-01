namespace money_problem.Domain;

public class Portfolio
{
    private readonly ICollection<Money> moneys = new List<Money>();

    public void Add(Money money) => this.moneys.Add(money);

    public Money Evaluate(Bank bank, Currency currency)
    {
        double convertedResult = 0;
        var missingExchangeRates = new List<MissingExchangeRateException>();
        foreach (Money money in this.moneys)
        {
            try
            {
                Money convertedMoney = bank.Convert(money, currency);
                convertedResult += convertedMoney.Amount;
            }
            catch (MissingExchangeRateException exception)
            {
                missingExchangeRates.Add(exception);
            }
        }
        
        if (missingExchangeRates.Any()) {
            throw new MissingExchangeRatesException(missingExchangeRates);
        }
        
        // Simply instantiate a new Money from here
        return new Money(convertedResult, currency);
    }
}