using System;
using System.Collections.Generic;
using System.Linq;
using FluentAssertions;
using FsCheck;
using FsCheck.Xunit;
using money_problem.Domain;
using Xunit;

namespace money_problem.Tests;

public class BankProperties
{
    private const double Tolerance = 0.01;
    private readonly Bank bank;

    private readonly Dictionary<(Currency From, Currency To), double> exchangeRates = new()
    {
        {(Currency.EUR, Currency.USD), 1.0567},
        {(Currency.USD, Currency.EUR), 0.9466},
        {(Currency.USD, Currency.KRW), 1302.0811},
        {(Currency.KRW, Currency.USD), 0.00076801737},
        {(Currency.EUR, Currency.KRW), 1368.51779},
        {(Currency.KRW, Currency.EUR), 0.00073},
    };

    public BankProperties()
    {
        this.bank = this.NewBankWithExchangeRates(this.exchangeRates);
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

    private Bank NewBankWithExchangeRates(Dictionary<(Currency From, Currency To), double> rates)
    {
        return rates.Aggregate(this.NewBank(),
            (aggregatedBank, pair) => aggregatedBank.AddExchangeRate(pair.Key.From, pair.Key.To, pair.Value));
    }

    private Bank NewBank()
    {
        var firstEntry = exchangeRates.First();
        return Bank.WithExchangeRate(firstEntry.Key.From, firstEntry.Key.To, firstEntry.Value);
    }
}