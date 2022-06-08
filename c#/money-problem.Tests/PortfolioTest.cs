using System;
using System.Collections.Generic;
using System.Linq;
using FluentAssertions;
using money_problem.Domain;
using Xunit;
using Xunit.Sdk;

namespace money_problem.Tests;

public class PortfolioTest
{
    private readonly Bank bank;

    public PortfolioTest()
    {
        this.bank = Bank
            .WithExchangeRate(Currency.EUR, Currency.USD, 1.2)
            .AddExchangeRate(Currency.USD, Currency.KRW, 1100);
    }

    [Fact(DisplayName = "5 USD + 10 EUR = 17 USD")]
    public void Add_ShouldAddMoneyInDollarAndEuro() =>
        PortfolioWith(new Money(5, Currency.USD), new Money(10, Currency.EUR))
            .EvaluateWithException(this.bank, Currency.USD)
            .Should()
            .Be(new Money(17, Currency.USD));

    [Fact(DisplayName = "1 USD + 1100 KRW = 2200 KRW")]
    public void Add_ShouldAddMoneyInDollarAndKoreanWons() =>
        PortfolioWith(new Money(1, Currency.USD), new Money(1100, Currency.KRW))
            .EvaluateWithException(this.bank, Currency.KRW)
            .Should()
            .Be(new Money(2200, Currency.KRW));

    [Fact(DisplayName = "5 USD + 10 EUR + 4 EUR = 21.8 USD")]
    public void Add_ShouldAddMoneyInDollarsAndMultipleAmountInEuros() =>
        PortfolioWith(new Money(5, Currency.USD), new Money(10, Currency.EUR), new Money(4, Currency.EUR))
            .EvaluateWithException(bank, Currency.USD)
            .Should()
            .Be(new Money(21.8, Currency.USD));

    [Fact(DisplayName = "Throws a MissingExchangeRatesException in case of missing exchange rates")]
    public void Add_ShouldThrowAMissingExchangeRatesException()
    {
        PortfolioWith(new Money(1, Currency.EUR), new Money(1, Currency.USD), new Money(1, Currency.KRW))
            .Evaluate(this.bank, Currency.EUR)
            .Failure
            .Should()
            .Be("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
    }

    [Fact(DisplayName = "5 USD + 10 USD = 15 USD")]
    public void Add_ShouldAddMoneyInTheSameCurrency()
    {
        PortfolioWith(new Money(5, Currency.USD), new Money(10, Currency.USD))
            .EvaluateWithException(bank, Currency.USD).Should().Be(new Money(15, Currency.USD));
    }

    private static Portfolio PortfolioWith(params Money[] moneys) =>
        moneys.Aggregate(new Portfolio(), (portfolio, money) => portfolio.Add(money));
}