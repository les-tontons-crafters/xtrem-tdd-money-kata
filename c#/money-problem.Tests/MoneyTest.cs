using FluentAssertions;
using money_problem.Domain;
using Xunit;

namespace money_problem.Tests;

public class MoneyTest
{
    [Fact(DisplayName = "10 EUR x 2 = 20 EUR")]
    public void MultiplyInEuros()
    {
        new Money(10, Currency.EUR)
            .Times(2)
            .Should()
            .Be(new Money(20, Currency.EUR));
    }

    [Fact(DisplayName = "4002 KRW / 4 = 1000.5 KRW")]
    public void DivideInKoreanWons()
    {
        new Money(4002, Currency.KRW)
            .Divide(4)
            .Should()
            .Be(new Money(1000.5, Currency.KRW));
    }
}