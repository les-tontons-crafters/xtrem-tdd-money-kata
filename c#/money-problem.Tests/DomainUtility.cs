using System.Linq;
using money_problem.Domain;

namespace money_problem.Tests;

public static class DomainUtility
{
    public static ExchangeRate CreateExchangeRate(Currency currency, double rate) =>
        (ExchangeRate) ExchangeRate.From(currency, rate).Case;

    public static Bank WithExchangeRates(Bank bank, params ExchangeRate[] rates) =>
        rates.Aggregate(bank, (runningValue, exchangeRate) => (Bank) runningValue.Add(exchangeRate).Case);
}