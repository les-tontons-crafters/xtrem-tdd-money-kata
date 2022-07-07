using System.Collections.Immutable;
using System.Collections.ObjectModel;
using LanguageExt;

namespace money_problem.Domain
{
    public sealed class Bank
    {
        private readonly ReadOnlyDictionary<string, double> _exchangeRates;

        private Bank(IDictionary<string, double> exchangeRates) => _exchangeRates = new ReadOnlyDictionary<string, double>(exchangeRates);

        public static Bank WithExchangeRate(Currency from, Currency to, double rate) => 
            new Bank(new Dictionary<string, double>()).AddExchangeRate(from, to, rate);

        public Bank AddExchangeRate(Currency from, Currency to, double rate)
        {
            var updatedRates = new Dictionary<string, double>(this._exchangeRates);
            var key = KeyFor(from, to);
            updatedRates.TryAdd(key, default);
            updatedRates[key] = rate;
            return new Bank(updatedRates);
        }

        private static string KeyFor(Currency from, Currency to) => $"{from}->{to}";
        
        private Money ConvertSafely(Money money, Currency to) =>
            to == money.Currency
                ? money
                : money with { Amount = money.Amount * _exchangeRates[KeyFor(money.Currency, to)], Currency = to};

        private bool CanConvert(Currency from, Currency to) =>
                from == to || _exchangeRates.ContainsKey(KeyFor(from, to));

        public Either<string, Money> Convert(Money money, Currency currency) =>
            this.CanConvert(money.Currency, currency)
                ? Either<string, Money>.Right(this.ConvertSafely(money, currency))
                : Either<string, Money>.Left($"{money.Currency}->{currency}");
    }
}
