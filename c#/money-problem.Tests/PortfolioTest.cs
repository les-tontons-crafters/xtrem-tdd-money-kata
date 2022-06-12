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
        PortfolioWith(5.Dollars(), 10.Euros())
            .Evaluate(this.bank, Currency.USD)
            .Money
            .Should()
            .Be(17.Dollars());

    [Fact(DisplayName = "1 USD + 1100 KRW = 2200 KRW")]
    public void Add_ShouldAddMoneyInDollarAndKoreanWons() =>
        PortfolioWith(1.Dollars(), 1100.KoreanWons())
            .Evaluate(this.bank, Currency.KRW)
            .Money
            .Should()
            .Be(2200.KoreanWons());

    [Fact(DisplayName = "5 USD + 10 EUR + 4 EUR = 21.8 USD")]
    public void Add_ShouldAddMoneyInDollarsAndMultipleAmountInEuros() =>
        PortfolioWith(5.Dollars(), 10.Euros(), 4.Euros())
            .Evaluate(bank, Currency.USD)
            .Money
            .Should()
            .Be(21.8.Dollars());

    [Fact(DisplayName = "Throws a MissingExchangeRatesException in case of missing exchange rates")]
    public void Add_ShouldThrowAMissingExchangeRatesException()
    {
        PortfolioWith(1.Euros(), 1.Dollars(), 1.KoreanWons())
            .Evaluate(this.bank, Currency.EUR)
            .Failure
            .Should()
            .Be("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
    }

    [Fact(DisplayName = "5 USD + 10 USD = 15 USD")]
    public void Add_ShouldAddMoneyInTheSameCurrency()
    {
        PortfolioWith(5.Dollars(), 10.Dollars())
            .Evaluate(bank, Currency.USD).Money.Should().Be(15.Dollars());
    }

    private static Portfolio PortfolioWith(params Money[] moneys) =>
        moneys.Aggregate(new Portfolio(), (portfolio, money) => portfolio.Add(money));
}