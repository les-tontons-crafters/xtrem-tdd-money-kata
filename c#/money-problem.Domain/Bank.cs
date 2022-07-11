using System.Collections.Immutable;
using System.Collections.ObjectModel;
using LanguageExt;

namespace money_problem.Domain
{
    public sealed class Bank
    {
        private readonly Map<string, double> _exchangeRates;

        private Bank(Map<string, double> exchangeRates) => this._exchangeRates = new Map<string, double>(exchangeRates);

        public static Bank WithExchangeRate(Currency from, Currency to, double rate) => 
            new Bank(new Map<string, double>()).AddExchangeRate(from, to, rate);

        public Bank AddExchangeRate(Currency from, Currency to, double rate) =>
            new Bank(new Map<string, double>(this._exchangeRates).AddOrUpdate(KeyFor(from, to), rate));

        private static string KeyFor(Currency from, Currency to) => $"{from}->{to}";
        
        private Money ConvertSafely(Money money, Currency to) =>
            to == money.Currency
                ? money
                : money with { Amount = money.Amount * this._exchangeRates.Find(KeyFor(money.Currency, to)).IfNone(0), Currency = to};

        private bool CanConvert(Currency from, Currency to) =>
                from == to || this._exchangeRates.ContainsKey(KeyFor(from, to));

        public Either<string, Money> Convert(Money money, Currency currency) =>
            this.CanConvert(money.Currency, currency)
                ? Either<string, Money>.Right(this.ConvertSafely(money, currency))
                : Either<string, Money>.Left($"{money.Currency}->{currency}");
    }
}
