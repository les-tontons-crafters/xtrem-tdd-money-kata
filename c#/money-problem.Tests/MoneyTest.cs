using FluentAssertions;
using money_problem.Domain;
using Xunit;

namespace money_problem.Tests;

public class MoneyTest
{
    [Fact(DisplayName = "10 EUR x 2 = 20 EUR")]
    public void MultiplyInEuros()
    {
        10.Euros()
            .Times(2)
            .Should()
            .Be(20.Euros());
    }

    [Fact(DisplayName = "4002 KRW / 4 = 1000.5 KRW")]
    public void DivideInKoreanWons()
    {
        4002.KoreanWons()
            .Divide(4)
            .Should()
            .Be(1000.5.KoreanWons());
    }
}