using System.Collections.Generic;
using System.Linq;
using FsCheck;
using FsCheck.Xunit;
using money_problem.Domain;

namespace money_problem.Tests;

public class BankProperties
{
    private readonly Bank bank;

    private readonly Dictionary<(Currency From, Currency To), double> exchangeRates = new()
    {
        {(Currency.EUR, Currency.USD), 1.2},
        {(Currency.USD, Currency.EUR), 0.82},
        {(Currency.USD, Currency.KRW), 1100},
        {(Currency.KRW, Currency.USD), 0.0009},
        {(Currency.EUR, Currency.KRW), 1344},
        {(Currency.KRW, Currency.EUR), 0.00073},
    };

    public BankProperties()
    {
        this.bank = this.NewBankWithExchangeRates(this.exchangeRates);
    }

    [Property]
    private Property ConvertInSameCurrencyShouldReturnOriginalMoney(Money originalAmount) =>
        (originalAmount == this.bank.Convert(originalAmount, originalAmount.Currency)).ToProperty();
    
    [Property]
    private Property RoundTripping(Money originalAmount, Currency currency) =>
        (originalAmount == this.bank
            .Convert(originalAmount, currency)
            .Bind(convertedMoney => this.bank.Convert(convertedMoney, originalAmount.Currency))).ToProperty();

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