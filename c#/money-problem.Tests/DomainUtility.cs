using money_problem.Domain;

namespace money_problem.Tests;

public static class DomainUtility
{
    public static NewExchangeRate CreateExchangeRate(Currency currency, double rate) =>
        (NewExchangeRate) NewExchangeRate.From(currency, rate).Case;
}