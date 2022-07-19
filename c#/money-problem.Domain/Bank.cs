using LanguageExt;

namespace money_problem.Domain
{
    public sealed class Bank
    {
        private readonly Seq<ExchangeRate> _exchangeRates;

        private Bank(Seq<ExchangeRate> exchangeRates) => this._exchangeRates = new Seq<ExchangeRate>(exchangeRates);

        private Money ConvertSafely(Money money, Currency to) =>
            to == money.Currency
                ? money
                : money with
                {
                    Amount = money.Amount * this._exchangeRates
                        .Find(exchange => exchange.IsSameExchange(ExchangeRate.Default(money.Currency, to)))
                        .Map(exchange => exchange.Rate)
                        .IfNone(0),
                    Currency = to,
                };

        private bool CanConvert(Currency from, Currency to) =>
            from == to || this._exchangeRates.Any(exchange => exchange.IsSameExchange(ExchangeRate.Default(from, to)));

        public Either<string, Money> Convert(Money money, Currency currency) =>
            this.CanConvert(money.Currency, currency)
                ? Either<string, Money>.Right(this.ConvertSafely(money, currency))
                : Either<string, Money>.Left($"{money.Currency}->{currency}");

        public static Bank WithExchangeRates(params ExchangeRate[] exchanges) =>
            new Bank(exchanges.ToSeq());

        public Bank AddExchangeRate(ExchangeRate exchange) =>
            new(new Seq<ExchangeRate>(this._exchangeRates.Filter(element => !element.IsSameExchange(exchange)))
                .Add(exchange));
    }
}