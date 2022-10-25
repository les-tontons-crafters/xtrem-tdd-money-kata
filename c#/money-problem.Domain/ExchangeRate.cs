using LanguageExt;

namespace money_problem.Domain;

public struct ExchangeRate
{
    private ExchangeRate(Currency currency, double rate)
    {
        this.Currency = currency;
        this.Rate = rate;
    }

    public Currency Currency { get; }

    public double Rate { get; }

    public static Either<Error, ExchangeRate> From(Currency currency, double rate) =>
        IsValidRate(rate)
            ? Either<Error, ExchangeRate>.Right(new ExchangeRate(currency, rate))
            : Either<Error, ExchangeRate>.Left(new Error("Exchange rate should be greater than 0."));

    private static bool IsValidRate(double rate) => rate > 0;

    public static ExchangeRate Default(Currency currency) => new(currency, default);

    public double GetReversedRate() => 1 / this.Rate;
}