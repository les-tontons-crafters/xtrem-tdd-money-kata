using LanguageExt;

namespace money_problem.Domain;

public struct NewExchangeRate
{
    private NewExchangeRate(Currency currency, double rate)
    {
        this.Currency = currency;
        this.Rate = rate;
    }

    public Currency Currency { get; }

    public double Rate { get; }

    public static Either<Error, NewExchangeRate> From(Currency currency, double rate) =>
        IsValidRate(rate)
            ? Either<Error, NewExchangeRate>.Right(new NewExchangeRate(currency, rate))
            : Either<Error, NewExchangeRate>.Left(new Error("Exchange rate should be greater than 0."));

    private static bool IsValidRate(double rate) => rate > 0;

    public static NewExchangeRate Default(Currency currency) => new(currency, default);

    public double GetReversedRate() => 1 / this.Rate;
}