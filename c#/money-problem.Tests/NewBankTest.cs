using FluentAssertions;
using FluentAssertions.LanguageExt;
using money_problem.Domain;
using Xunit;

namespace money_problem.Tests;

public class NewBankTest
{
    [Fact]
    public void ConvertInDollarsFromEuros() =>
        NewBank
            .WithPivotCurrency(Currency.EUR)
            .Add(DomainUtility.CreateExchangeRate(Currency.USD, 1.2))
            .Map(bank => bank.Convert(10d.Euros(), Currency.USD))
            .Should()
            .Be(12d.Dollars());
    
    [Fact]
    public void ConvertInDollarsFromEurosWithUpdatedRate() =>
        NewBank
            .WithPivotCurrency(Currency.EUR)
            .Add(DomainUtility.CreateExchangeRate(Currency.USD, 1.1))
            .Bind(bank => bank.Add(DomainUtility.CreateExchangeRate(Currency.USD, 1.2)))
            .Map(bank => bank.Convert(10d.Euros(), Currency.USD))
            .Should()
            .Be(12d.Dollars());
    
    [Fact]
    public void ConvertThroughPivotCurrency() =>
        NewBank
            .WithPivotCurrency(Currency.EUR)
            .Add(DomainUtility.CreateExchangeRate(Currency.USD, 1.2))
            .Bind(bank => bank.Add(DomainUtility.CreateExchangeRate(Currency.KRW, 1344)))
            .Map(bank => bank.Convert(10d.Dollars(), Currency.KRW))
            .Should()
            .Be(11220d.KoreanWons());
}