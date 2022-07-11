namespace money_problem.Domain;

public record Money(double Amount, Currency Currency)
{
    public Money Times(int times) => this with {Amount = this.Amount * times};
    public Money Divide(int divisor) => this with {Amount = this.Amount / divisor};

    public static Money Empty(Currency currency) => new Money(default, currency);
}