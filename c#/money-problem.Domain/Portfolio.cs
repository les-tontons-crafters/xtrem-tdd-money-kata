namespace money_problem.Domain;

public class Portfolio
{
    private readonly Dictionary<Currency, ICollection<double>> moneys = new Dictionary<Currency, ICollection<double>>();
    public void Add(double amount, Currency currency)
    {
        if (!this.moneys.ContainsKey(currency))
        {
            this.moneys.Add(currency, new List<double>());
        }
        
        this.moneys[currency].Add(amount);
    }

    public double Evaluate(Bank bank, Currency currency)
    {
        double convertedResult = 0;
        var missingExchangeRates = new List<MissingExchangeRateException>();
        foreach (KeyValuePair<Currency, ICollection<double>> entry in this.moneys)
        {
            foreach (double amount in entry.Value)
            {
                try
                {
                    double convertedAmount = bank.Convert(amount, entry.Key, currency);
                    convertedResult += convertedAmount;
                }
                catch (MissingExchangeRateException exception)
                {
                    missingExchangeRates.Add(exception);
                }
            }
        }
        
        if (missingExchangeRates.Any()) {
            throw new MissingExchangeRatesException(missingExchangeRates);
        }
        
        return convertedResult;
    }
}