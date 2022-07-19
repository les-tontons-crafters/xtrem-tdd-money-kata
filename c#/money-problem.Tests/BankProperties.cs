using System;
using FluentAssertions;
using FsCheck;
using FsCheck.Xunit;
using LanguageExt;
using money_problem.Domain;
using Xunit;

namespace money_problem.Tests;

public class BankProperties
{
    private const double Tolerance = 0.01;
    private readonly Bank bank;

    private readonly Seq<ExchangeRate> exchangeRates = new Seq<ExchangeRate>()
        .Add(new ExchangeRate(Currency.EUR, Currency.USD, 1.0567))
        .Add(new ExchangeRate(Currency.USD, Currency.EUR, 0.9466))
        .Add(new ExchangeRate(Currency.USD, Currency.KRW, 1302.0811))
        .Add(new ExchangeRate(Currency.KRW, Currency.USD, 0.00076801737))
        .Add(new ExchangeRate(Currency.EUR, Currency.KRW, 1368.51779))
        .Add(new ExchangeRate(Currency.KRW, Currency.EUR, 0.00073));

    public BankProperties()
    {
        this.bank = this.exchangeRates.Aggregate(Bank.WithExchangeRates(),
            (aggregatedBank, exchange) => aggregatedBank.AddExchangeRate(exchange));
        Arb.Register<MoneyGenerator>();
    }

    [Property]
    private Property ConvertInSameCurrencyShouldReturnOriginalMoney(Money originalAmount) =>
        (originalAmount == this.bank.Convert(originalAmount, originalAmount.Currency)).ToProperty();

    [Property]
    private Property RoundTripping(Money originalAmount, Currency currency) =>
        this.IsRoundTripConversionSuccessful(originalAmount, currency)
            .ToProperty();

    private bool IsRoundTripConversionSuccessful(Money originalAmount, Currency currency) =>
        this.bank
            .Convert(originalAmount, currency)
            .Bind(convertedMoney => this.bank.Convert(convertedMoney, originalAmount.Currency))
            .Map(convertedAmount => this.VerifyTolerance(originalAmount, convertedAmount))
            .IfLeft(false);

    [Fact(DisplayName = "Money { Amount = 0,9632981371199433, Currency = USD }, KRW")]
    public void RoundTripError()
    {
        var originalAmount = new Money(0.9632981371199433, Currency.USD);
        this.IsRoundTripConversionSuccessful(originalAmount, Currency.KRW)
            .Should()
            .BeTrue();
    }

    private bool VerifyTolerance(Money originalAmount, Money convertedAmount) =>
        Math.Abs(originalAmount.Amount - convertedAmount.Amount) <= GetTolerance(originalAmount);

    private double GetTolerance(Money originalMoney) => Math.Abs(originalMoney.Amount * Tolerance);
}