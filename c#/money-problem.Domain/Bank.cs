using System.Collections.Immutable;

namespace money_problem.Domain
{
    public sealed class Bank
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

        public double Convert(double amount, Currency from, Currency to) =>
            CanConvert(from, to)
                ? ConvertSafely(amount, from, to)
                : throw new MissingExchangeRateException(from, to);

        private double ConvertSafely(double amount, Currency from, Currency to) =>
            to == from
                ? amount
                : amount * _exchangeRates[KeyFor(from, to)];

        private bool CanConvert(Currency from, Currency to) =>
            from == to || _exchangeRates.ContainsKey(KeyFor(from, to));
    }
}