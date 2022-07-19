namespace money_problem.Domain;

public record ExchangeRate(Currency From, Currency To, double Rate)
{
    public bool IsSameExchange(ExchangeRate exchange) => this.From == exchange.From && this.To == exchange.To;

    public static ExchangeRate Default(Currency from, Currency to) => new(from, to, default);
}