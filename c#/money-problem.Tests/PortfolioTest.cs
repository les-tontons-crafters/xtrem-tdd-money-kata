using System;
using System.Collections.Generic;
using System.Linq;
using FluentAssertions;
using FluentAssertions.Extensions;
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
        PortfolioWith(5d.Dollars(), 10d.Euros())
            .Evaluate(this.bank, Currency.USD)
            .Should()
            .Be(17d.Dollars());

    [Fact(DisplayName = "1 USD + 1100 KRW = 2200 KRW")]
    public void Add_ShouldAddMoneyInDollarAndKoreanWons() =>
        PortfolioWith(1d.Dollars(), 1100d.KoreanWons())
            .Evaluate(this.bank, Currency.KRW)
            .Should()
            .Be(2200d.KoreanWons());

    [Fact(DisplayName = "5 USD + 10 EUR + 4 EUR = 21.8 USD")]
    public void Add_ShouldAddMoneyInDollarsAndMultipleAmountInEuros() =>
        PortfolioWith(5d.Dollars(), 10d.Euros(), 4d.Euros())
            .Evaluate(bank, Currency.USD)
            .Should()
            .Be(21.8.Dollars());

    [Fact(DisplayName = "Throws a MissingExchangeRatesException in case of missing exchange rates")]
    public void Add_ShouldThrowAMissingExchangeRatesException()
    {
        Action act = () => PortfolioWith(1d.Euros(), 1d.Dollars(), 1d.KoreanWons())
            .Evaluate(this.bank, Currency.EUR);
        act.Should().Throw<MissingExchangeRatesException>()
            .WithMessage("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
    }

    [Fact(DisplayName = "5 USD + 10 USD = 15 USD")]
    public void Add_ShouldAddMoneyInTheSameCurrency()
    {
        PortfolioWith(5d.Dollars(), 10d.Dollars())
            .Evaluate(bank, Currency.USD).Should().Be(15d.Dollars());
    }

    private static Portfolio PortfolioWith(params Money[] moneys) =>
        moneys.Aggregate(new Portfolio(), (portfolio, money) => portfolio.Add(money));
}
