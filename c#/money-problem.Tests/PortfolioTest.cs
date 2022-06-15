using System;
using System.Collections.Generic;
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
        this.bank = Bank.WithExchangeRate(Currency.EUR, Currency.USD, 1.2);
        bank.AddExchangeRate(Currency.USD, Currency.KRW, 1100);
    }

    [Fact(DisplayName = "5 USD + 10 EUR = 17 USD")]
    public void Add_ShouldAddMoneyInDollarAndEuro()
    {
        // Arrange
        Portfolio portfolio = new Portfolio();
        portfolio.Add(5d.Dollars());
        portfolio.Add(10d.Euros());

        // Act
        var evaluation = portfolio.Evaluate(bank, Currency.USD);

        // Assert
        evaluation.Should().Be(17d.Dollars());
    }

    [Fact(DisplayName = "1 USD + 1100 KRW = 2200 KRW")]
    public void Add_ShouldAddMoneyInDollarAndKoreanWons()
    {
        var portfolio = new Portfolio();
        portfolio.Add(1d.Dollars());
        portfolio.Add(1100d.KoreanWons());
        portfolio.Evaluate(bank, Currency.KRW).Should().Be(2200d.KoreanWons());
    }

    [Fact(DisplayName = "5 USD + 10 EUR + 4 EUR = 21.8 USD")]
    public void Add_ShouldAddMoneyInDollarsAndMultipleAmountInEuros()
    {
        var portfolio = new Portfolio();
        portfolio.Add(5d.Dollars());
        portfolio.Add(10d.Euros());
        portfolio.Add(4d.Euros());
        portfolio.Evaluate(bank, Currency.USD).Should().Be(21.8.Dollars());
    }

    [Fact(DisplayName = "Throws a MissingExchangeRatesException in case of missing exchange rates")]
    public void Add_ShouldThrowAMissingExchangeRatesException()
    {
        var portfolio = new Portfolio();
        portfolio.Add(1d.Euros());
        portfolio.Add(1d.Dollars());
        portfolio.Add(1d.KoreanWons());
        Action act = () => portfolio.Evaluate(this.bank, Currency.EUR);
        act.Should().Throw<MissingExchangeRatesException>()
            .WithMessage("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
    }

    [Fact(DisplayName = "5 USD + 10 USD = 15 USD")]
    public void Add_ShouldAddMoneyInTheSameCurrency()
    {
        var portfolio = new Portfolio();
        portfolio.Add(5d.Dollars());
        portfolio.Add(10d.Dollars());
        portfolio.Evaluate(bank, Currency.USD).Should().Be(15d.Dollars());
    }
}