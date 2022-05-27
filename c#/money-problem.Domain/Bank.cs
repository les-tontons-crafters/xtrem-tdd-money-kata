using System.Collections.Immutable;

namespace money_problem.Domain
{
    public class Bank
    {
        private readonly Dictionary<string, double> _exchangeRates;

        private Bank(Dictionary<string, double> exchangeRates) => _exchangeRates = exchangeRates;

        public static Bank WithExchangeRate(Currency from, Currency to, double rate)
        {
            var bank = new Bank(new Dictionary<string, double>());
            bank.AddExchangeRate(from, to, rate);

            return bank;
        }

        public void AddExchangeRate(Currency from, Currency to, double rate) 
            => _exchangeRates[KeyFor(from, to)] =  rate;

        private static string KeyFor(Currency from, Currency to) => $"{from}->{to}";

        public double Convert(double amount, Currency fromCurrency, Currency toCurrency) =>
            CanConvert(fromCurrency, toCurrency)
                ? ConvertSafely(amount, fromCurrency, toCurrency)
                : throw new MissingExchangeRateException(fromCurrency, toCurrency);

        private double ConvertSafely(double amount, Currency fromCurrency, Currency toCurrency) =>
            toCurrency == fromCurrency
                ? amount
                : amount * _exchangeRates[KeyFor(fromCurrency, toCurrency)];

        private bool CanConvert(Currency from, Currency to) =>
            from == to || _exchangeRates.ContainsKey(KeyFor(from, to));
    }
}