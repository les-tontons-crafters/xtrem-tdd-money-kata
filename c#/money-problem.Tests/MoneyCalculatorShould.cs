using FluentAssertions;
using money_problem.Domain;
using Xunit;
using static money_problem.Domain.Currency;

namespace money_problem.Tests
{
    public class MoneyShould
    {
        [Fact(DisplayName = "5 USD + 10 USD = 15 USD")]
        public void Add()
        {
            double? result = MoneyCalculator.Add(5, USD, 10);
            result.Should()
                .NotBeNull();
        }
        
        [Fact(DisplayName = "10 EUR x 2 = 20 EUR")]
        public void Multiply()
        {
            MoneyCalculator
                .Times(10, EUR, 2)
                .Should()
                .Be(20d);
        }

        [Fact(DisplayName = "4002 KRW / 4 = 1000.5 KRW")]
        public void Divide()
        {
            MoneyCalculator
                .Divide(4002, KRW, 4)
                .Should()
                .Be(1000.5d);
        }
    }
}