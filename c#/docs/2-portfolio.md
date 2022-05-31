# Implement Portfolio
Implement the 2 new features :

```text
5 USD + 10 EUR = 17 USD
1 USD + 1100 KRW = 2200 KRW
```

## Write our first test

```java
public class PortfolioTest
{
    private readonly Bank bank = Bank.WithExchangeRate(Currency.EUR, Currency.USD, 1.2);

    [Fact(DisplayName = "5 USD + 10 EUR = 17 USD")]
    public void Add_ShouldAddMoneyInDollarAndEuro()
    {
        // Arrange
        Portfolio portfolio = new Portfolio();
        portfolio.Add(5, Currency.USD);
        portfolio.Add(10, Currency.EUR);

        // Act
        var evaluation = portfolio.Evaluate(bank, Currency.USD);

        // Assert
        evaluation.Should().Be(17);
    }
}
```

- From your IDE you should see your code like this

![Failing multi same currencies](img/PortfolioFirstFailingTest.png)

- Congratulations you have a first failing test (You don't compile)
- Now we have a failing test : `Make it pass as fast as possible`
	- We start by using the power of our IDE and `generate code from usage`

![Generate code from usage](img/PortfolioGenerateCodeFromUsage.png)

- Generated code :

```c#
public class Portfolio
{
    public void Add(double amount, Currency currency)
    {
        throw new System.NotImplementedException();
    }

    public double Evaluate(Bank bank, Currency currency)
    {
        throw new System.NotImplementedException();
    }
}
```	

- Then we can use the strategy for that is called `Fake It 'Til You Make It` (more about it [here](https://dzone.com/articles/three-modes-of-tdd))


```c#
public double evaluate(Bank bank, Currency currency) {
	// Fake It 'Til You Make It
    return 17;
}
```

Where we are:

```text
✅ 5 USD + 10 EUR = 17 USD
1 USD + 1100 KRW = 2200 KRW
5 USD + 10 EUR + 4 EUR = 21.8 USD
Improve error handling
```

## Handle currencies in KoreanWons
- Let's write a new failing test

```c#
@Test
@DisplayName("1 USD + 1100 KRW = 2200 KRW")
void shouldAddMoneyInDollarsAndKoreanWons() throws MissingExchangeRateException {
    var portfolio = new Portfolio();
    portfolio.add(1, USD);
    portfolio.add(1100, KRW);

    assertThat(portfolio.evaluate(bank, KRW))
            .isEqualTo(2200);
}
```

- The test is failing because we have faked the result of the `evaluated` method
	- Here we use what we call `triangulation`
		- We start by hardcoding the result
		- We provide another test that leads us closer to the final solution

```c#
public class Portfolio
{
    private readonly Dictionary<Currency, ICollection<double>> moneys = new Dictionary<Currency, ICollection<double>>();
    public void Add(double amount, Currency currency)
    {
        if (!this.moneys.ContainsKey(currency))
        {
            this.moneys.Add(currency, new List<double> { amount });
        }
        else
        {
            this.moneys[currency] = new List<double> { amount };
        }
    }

    public double Evaluate(Bank bank, Currency currency)
    {
        double convertedResult = 0;
        foreach (KeyValuePair<Currency, ICollection<double>> entry in this.moneys)
        {
            foreach (double amount in entry.Value)
            {
                double convertedAmount = bank.Convert(amount, entry.Key, currency);
                convertedResult += convertedAmount;
            }
        }
        
        return convertedResult;
    }
}
```

- Any refactoring ?
	- In the tests, we could centralize the exchange rates setup

```java
private readonly Bank bank;

public PortfolioTest()
{
	this.bank = Bank.WithExchangeRate(Currency.EUR, Currency.USD, 1.2);
	bank.AddExchangeRate(Currency.USD, Currency.KRW, 1100);
}
```

- New stuff / refactoring ideas are emerging from the current implementation :
	- If we have the same currency twice we have a problem in the `add` method
		- We need to increase our confidence by adding a new test on it
	- Missing Exchange rate -> how to improve error handling?
- Let's add new test cases for our portfolio :

```text
✅ 5 USD + 10 EUR = 17 USD
✅ 1 USD + 1100 KRW = 2200 KRW
5 USD + 10 EUR + 4 EUR = 21.8 USD
Improve error handling
```

## Portfolio containing amounts in same currencies
- Let's write a red test

```c#
[Fact(DisplayName = "5 USD + 10 EUR + 4 EUR = 21.8 USD")]
public void Add_ShouldAddMoneyInDollarsAndMultipleAmountInEuros()
{
	var portfolio = new Portfolio();
	portfolio.Add(5, Currency.USD);
	portfolio.Add(10, Currency.EUR);
	portfolio.Add(4, Currency.EUR);
	portfolio.Evaluate(bank, Currency.USD).Should().Be(21.8);
}
```

![Failing multi same currencies](img/PortfolioFailingCurrencies.png)

- Make it pass by refactoring the `add` method

```c#
public void Add(double amount, Currency currency)
{
	if (!this.moneys.ContainsKey(currency))
	{
		this.moneys.Add(currency, new List<double>());
	}
	
	this.moneys[currency].Add(amount);
}
```

```text
✅ 5 USD + 10 EUR = 17 USD
✅ 1 USD + 1100 KRW = 2200 KRW
✅ 5 USD + 10 EUR + 4 EUR = 21.8 USD
Improve error handling
```

## Improve error handling
- Here we may improve error handling
	- If we have multiple missing exchange rates we return the information only for the first missing one...

```c#
[Fact(DisplayName = "Throws a MissingExchangeRatesException in case of missing exchange rates")]
public void Add_ShouldThrowAMissingExchangeRatesException()
{
	var portfolio = new Portfolio();
	portfolio.Add(1, Currency.EUR);
	portfolio.Add(1, Currency.USD);
	portfolio.Add(1, Currency.KRW);
	Action act = () => portfolio.Evaluate(this.bank, Currency.EUR));
	act.Should().Throw<MissingExchangeRatesException>()
		.WithMessage("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");        
}
```

- Generate the Exception class from the test

```c#
public class MissingExchangeRatesException : Exception
{
    public MissingExchangeRatesException(List<MissingExchangeRateException> missingExchangeRates)
        : base("Missing exchange rate(s): [USD->EUR],[KRW->EUR]")
    {
        
    }
}
```

- Adapt our evaluation to pass the test

```c#
public double Evaluate(Bank bank, Currency currency)
{
	double convertedResult = 0;
	var missingExchangeRates = new List<MissingExchangeRateException>();
	foreach (KeyValuePair<Currency, ICollection<double>> entry in this.moneys)
	{
		foreach (double amount in entry.Value)
		{
			try
			{
				double convertedAmount = bank.Convert(amount, entry.Key, currency);
				convertedResult += convertedAmount;
			}
			catch (MissingExchangeRateException exception)
			{
				missingExchangeRates.Add(exception);
			}
		}
	}
	
	if (missingExchangeRates.Any()) {
		throw new MissingExchangeRatesException(missingExchangeRates);
	}
	
	return convertedResult;
}
```

- Let's adapt our tests accordingly
	- The `evaluate` method is now throwing only `MissingExchangeRatesException`

```java
@Test
@DisplayName("5 USD + 10 EUR + 4 EUR = 21.8 USD")
void shouldAddMoneyInDollarsAndMultipleAmountInEuros() throws MissingExchangeRatesException {
    var portfolio = new Portfolio();
    portfolio.add(5, USD);
    portfolio.add(10, EUR);
    portfolio.add(4, EUR);

    assertThat(portfolio.evaluate(bank, USD))
            .isEqualTo(21.8);
}

@Test
@DisplayName("Throws a MissingExchangeRatesException in case of missing exchange rates")
void shouldThrowAMissingExchangeRatesException() {
    var portfolio = new Portfolio();
    portfolio.add(1, EUR);
    portfolio.add(1, USD);
    portfolio.add(1, KRW);

    assertThatThrownBy(() -> portfolio.evaluate(bank, EUR))
            .isInstanceOf(MissingExchangeRatesException.class)
            .hasMessage("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
}
```

- The tests are now all green
- Let's refactor now
	- We have some harcoded values in the new `MissingExchangeRatesException` class

```c#
public class MissingExchangeRatesException : Exception
{
    public MissingExchangeRatesException(List<MissingExchangeRateException> missingExchangeRates)
        : base($"Missing exchange rate(s): {GetMissingRates(missingExchangeRates)}")
    {
    }

    private static string GetMissingRates(List<MissingExchangeRateException> missingRates) => missingRates
        .Select(exception => $"[{exception.Message}]")
        .Aggregate((r1, r2) => $"{r1},{r2}");
}
```

```text
✅ 5 USD + 10 EUR = 17 USD
✅ 1 USD + 1100 KRW = 2200 KRW
✅ 5 USD + 10 EUR + 4 EUR = 21.8 USD
✅ Improve error handling
```

- We have a code that grows in our `Portfolio`
	- Let's keep it for the coming constraints 😊

## Reflect
During this iteration we have implemented a `Portfolio` that allows to add different amounts in different currencies. Let's take a look at our test cases :

![Add is not necessary anymore](img/PortfolioMoveAddToPortfolio.png)

- With our new features it would make sense to use only `Portfolio` to add moneys together
- Let's move this test in our `PortfolioTest` suite

```c#
[Fact(DisplayName = "5 USD + 10 USD = 15 USD")]
public void Add_ShouldAddMoneyInTheSameCurrency()
{
	var portfolio = new Portfolio();
	portfolio.Add(5, Currency.USD);
	portfolio.Add(10, Currency.USD);
	portfolio.Evaluate(bank, Currency.USD).Should().Be(15);
}
```

- Refactor :
	- Remove the `add` method from our `MoneyCalculator`

`Always put the same attention on your test code than on your production code`
