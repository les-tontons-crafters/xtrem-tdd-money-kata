using System.Collections.Generic;
using FluentAssertions.LanguageExt;
using money_problem.Domain;
using Xunit;

namespace money_problem.Tests;

public class BankTest
{
    private const Currency PivotCurrency = Currency.EUR;
    private readonly Bank bank;

    public BankTest() => this.bank = Bank.WithPivotCurrency(PivotCurrency);

    public static IEnumerable<object[]> ExamplesForConvertThroughPivotCurrency =>
        new List<object[]>
        {
            new object[] {10d.Dollars(), Currency.KRW, 11200d.KoreanWons()},
            new object[] {(-1d).Dollars(), Currency.KRW, (-1120d).KoreanWons()},
            new object[] {1000d.KoreanWons(), Currency.USD, 0.8928571428571427.Dollars()},
        };

    public static IEnumerable<object[]> ExamplesForConvertThroughExchangeRate =>
        new List<object[]>
        {
            new object[] {87d.Dollars(), Currency.EUR, 72.5d.Euros()},
            new object[] {1009765d.KoreanWons(), Currency.EUR, 751.313244047619d.Euros()},
            new object[] {10d.Euros(), Currency.USD, 12d.Dollars()},
            new object[] {10d.Euros(), Currency.KRW, 13440d.KoreanWons()},
        };

    [Theory]
    [MemberData(nameof(ExamplesForConvertThroughExchangeRate))]
    public void ConvertConvertThroughExchangeRate(Money money, Currency currency, Money expected) =>
        DomainUtility.WithExchangeRates(
                this.bank,
                DomainUtility.CreateExchangeRate(Currency.USD, 1.2),
                DomainUtility.CreateExchangeRate(Currency.KRW, 1344))
            .Convert(money, currency)
            .Should()
            .Be(expected);

    [Fact]
    public void ConvertInDollarsFromEurosWithUpdatedRate() =>
        DomainUtility.WithExchangeRates(
                this.bank,
                DomainUtility.CreateExchangeRate(Currency.USD, 1.1),
                DomainUtility.CreateExchangeRate(Currency.USD, 1.2))
            .Convert(10d.Euros(), Currency.USD)
            .Should()
            .Be(12d.Dollars());

    [Theory]
    [MemberData(nameof(ExamplesForConvertThroughPivotCurrency))]
    public void ConvertThroughPivotCurrency(Money money, Currency currency, Money expected) =>
        DomainUtility.WithExchangeRates(
                this.bank,
                DomainUtility.CreateExchangeRate(Currency.USD, 1.2),
                DomainUtility.CreateExchangeRate(Currency.KRW, 1344))
            .Convert(money, currency)
            .Should()
            .Be(expected);
}