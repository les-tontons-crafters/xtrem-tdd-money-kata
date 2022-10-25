## Redesign the Bank
Based on our discoveries from the `example mapping` workshop, we can work on redesigning our `Bank`.
Again, our examples will drive the implementation.

One significant impact is modifying the `Bank`at this stage will have a massive impact on the codebase.

### Sprout Technique
One way to avoid it is to use a technique called [Sprout Technique](https://understandlegacycode.com/blog/key-points-of-working-effectively-with-legacy-code/#1-the-sprout-technique) from [Michael Feathers](https://wiki.c2.com/?MichaelFeathers) in his book [Working Effectively with Legacy Code](https://www.oreilly.com/library/view/working-effectively-with/0131177052/):
- Create your code somewhere else.
  - Keep applying Test-Driven Development based on our new use cases.
- Identify where to call that code from the existing one: the `insertion point`.
- Call your code from the existing code.

> Let's try!

### Implement the New Bank
We're going to iterate upon the example mapping outcome.

#### Define pivot currency

![Define Pivot Currency](img/BankRedesignPivotCurrency.png)

From the business point of view, those rules are fundamental and should be at the heart of our system.
To implement those, we can encapsulate our class by making it impossible `by design` to change the pivot currency of an
existing `Bank` instance.

We don't have test cases here, but it gives us ideas for preserving the integrity of our system.

`Property-Based Testing` is a good candidate to verify these rules.
We will follow the main rules as shown below.

#### Add an exchange rate

![Add an exchange rate](img/BankRedesignAddExchangeRate.png)

> Add an exchange rate for the Pivot Currency

```text
for all (currency, rate)
CreateBankWithPivotCurrency(currency)
    .Add(new ExchangeRate(rate, currency)) should return an error("Can not add an exchange rate for the pivot currency")
```

Let's start to work on this property first.

:red_circle: As usual, we start with a failing test/property on a new bank implementation.

```csharp
[Property]
private Property CannotAddExchangeRateForThePivotCurrencyOfTheBank() =>
    Prop.ForAll(
        Arb.From<Currency>(),
        Arb.From<double>().MapFilter(_ => _, rate => rate > 0),
        (currency, rate) =>
            NewBank
            .WithPivotCurrency(currency)
            .Add(new NewExchangeRate(currency, rate)) == Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency.")));
```

We have quite some code to generate here.
As usual, we are going to use our IDE to do so.

Also, we will have to adapt our existing record `ExchangeRate`.
Indeed, we won't have to provide the source currency anymore because it will always be our pivot currency.

```csharp
public class NewBank
{
    public static NewBank WithPivotCurrency(Currency currency)
    {
        throw new NotImplementedException();
    }

    public Either<Error,NewBank> AddExchangeRate(NewExchangeRate exchangeRate)
    {
        throw new NotImplementedException();
    }
}

public record Error(string Message);

public record NewExchangeRate(Currency To, double Rate);
```

:green_circle: We can fake the result to pass our test.

```csharp
public static NewBank WithPivotCurrency(Currency currency)
{
    return new NewBank();
}

public Either<Error,NewBank> Add(NewExchangeRate exchangeRate)
{
    return Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency."));
}
```

:large_blue_circle: Even with our trivial implementation, we can already reduce the noise in our test.

```csharp
[Property]
private Property CannotAddExchangeRateForThePivotCurrencyOfTheBank() =>
    Prop.ForAll(
        Arb.From<Currency>(),
        GetValidRates(),
        (currency, rate) =>
            AddShouldReturnErrorForSameCurrencyAsPivot(currency, rate,
                "Cannot add an exchange rate for the pivot currency."));

private static Arbitrary<double> GetValidRates() => Arb.From<double>().MapFilter(_ => _, rate => rate > 0);

private static bool AddShouldReturnErrorForSameCurrencyAsPivot(Currency currency, double rate, string message) =>
    NewBank
        .WithPivotCurrency(currency)
        .Add(new NewExchangeRate(currency, rate)) == Either<Error, NewBank>.Left(new Error(message));
```

> Add an invalid rate for a Currency

:red_circle: Let's add a second property regarding an `Exchange rate` that should never be negative or equal to 0.

```text
for all (pivotCurrency, currency, negativeOr0Double)
such that currency != pivotCurrency
CreateBankWithPivotCurrency(pivotCurrency)
    .Add(new ExchangeRate(negativeOr0Double, currency)) should return error("Exchange rate should be greater than 0")
```

It should be a responsibility of the `ExchangeRate` data structure so let's write this property aside from the ones of
the `Bank`.

```csharp
[Property]
public Property CannotUseNegativeDoubleOrZeroAsExchangeRate() =>
    Prop.ForAll(
        Arb.From<Currency>(),
        Arb.From<double>().MapFilter(_ => _, rate => rate <= 0),
        (currency, rate) =>NewExchangeRate.From(currency, rate) ==
            Either<Error, NewExchangeRate>.Left(new Error("Exchange rate should be greater than 0.")));
```

:green_circle: Create the method and fake its behaviour for now.
We want to ensure that we can create only valid exchange rates.
Hence we have to force the usage of the `From` factory method.
We have to change our `ExchangeRate` record to a struct.

This principle is called [`parse, don't validate`](https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/).

Once we instantiate an object, **we know for sure that it is valid**.
It means we will **never** have an invalid `ExchangeRate`.

If you use only primitive types, this property is hard to achieve, and you will have to make a lot of validation in
different places inside your system.

```csharp
public class ExchangeRate {
    private double rate;
    private Currency currency;

    public ExchangeRate(double rate, Currency currency) {
        this.rate = rate;
        this.currency = currency;
    }

    public static Either<Error, ExchangeRate> from(double rate, Currency currency) {
        return Left(new Error("Exchange rate should be greater than 0"));
    }
}
```

The constructor have to stay public for now because it's being used in the Bank properties.

:large_blue_circle: Same as before, we can reduce the noise

```csharp
[Property]
public Property CannotUseNegativeDoubleOrZeroAsExchangeRate() =>
    Prop.ForAll(
        Arb.From<Currency>(),
        GetInvalidRates(),
        (currency, rate) =>
            ExchangeRateShouldReturnError(currency, rate, "Exchange rate should be greater than 0."));

private static Arbitrary<double> GetInvalidRates() => Arb.From<double>().MapFilter(_ => _, rate => rate <= 0);

private static bool ExchangeRateShouldReturnError(Currency currency, double rate, string message) =>
    NewExchangeRate.From(currency, rate) ==
    Either<Error, NewExchangeRate>.Left(new Error(message));
```

As mentioned before, we cannot use the factory method in the `NewBankProperties` until we finalize it.


> Add an exchange rate

```text
for all (pivotCurrency, currency, positiveDouble)
such that currency != pivotCurrency
CreateBankWithPivotCurrency(pivotCurrency)
    .Add(new ExchangeRate(positiveDouble, currency)) should return success
```

:red_circle: Let's write a new test to implement this behaviour.

```csharp
[Property]
private Property CanAddExchangeRateForDifferentCurrencyThanPivot() =>
    Prop.ForAll<Currency, Currency, double>(
        Arb.From<Currency>(),
        Arb.From<Currency>(),
        GetValidRates(),
        (pivot, currency, rate) =>
        NewBank
            .WithPivotCurrency(pivot)
            .Add(new NewExchangeRate(currency, rate))
            .IsRight
        .When(pivot != currency));
```

:green_circle: Improve the `ExchangeRate` design to move on.

```csharp
public class NewBank
{
    private Currency pivotCurrency;

    private NewBank(Currency pivotCurrency)
    {
        this.pivotCurrency = pivotCurrency;
    }

    public static NewBank WithPivotCurrency(Currency currency) { ... }

    public Either<Error,NewBank> Add(NewExchangeRate exchangeRate)
    {
        if (exchangeRate.Currency != this.pivotCurrency)
        {
            return Either<Error, NewBank>.Right(this);
        }
        
        return Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency."));
    }
}

public struct NewExchangeRate
{
    private Currency currency;
    private double rate;

    public NewExchangeRate(Currency currency, double rate) { ... }

    public Currency Currency => this.currency;

    public static Either<Error, NewExchangeRate> From(Currency currency, double rate) { ... }
}
```

:large_blue_circle: We can refactor both our test and our implementation.

```csharp
[Property]
private Property CanAddExchangeRateForDifferentCurrencyThanPivot() =>
    Prop.ForAll(
        Arb.From<Currency>(),
        Arb.From<Currency>(),
        GetValidRates(),
        (pivot, currency, rate) =>
            AddShouldReturnBankForExchangeRateForDifferentCurrencyThanPivot(pivot, currency, rate)
                .When(pivot != currency));

private static bool AddShouldReturnBankForExchangeRateForDifferentCurrencyThanPivot(Currency pivot,
    Currency currency, double rate) =>
    NewBank
        .WithPivotCurrency(pivot)
        .Add(new NewExchangeRate(currency, rate))
        .IsRight;
```

```csharp
public class NewBank
{
    private readonly Currency pivotCurrency;

    private NewBank(Currency pivotCurrency) => this.pivotCurrency = pivotCurrency;

    public static NewBank WithPivotCurrency(Currency currency) => new(currency);

    public Either<Error, NewBank> Add(NewExchangeRate exchangeRate) =>
        this.IsPivotCurrency(exchangeRate.Currency)
            ? Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency."))
            : Either<Error, NewBank>.Right(this);

    private bool IsPivotCurrency(Currency currency) => currency == this.pivotCurrency;
}

public struct NewExchangeRate
{
    private double rate;

    public NewExchangeRate(Currency currency, double rate)
    {
        this.Currency = currency;
        this.rate = rate;
    }

    public Currency Currency { get; }

    public static Either<Error, NewExchangeRate> From(Currency currency, double rate) => Either<Error, NewExchangeRate>.Left(new Error("Exchange rate should be greater than 0."));
}
```

:red_circle: We can finalize the implementation of our factory method.

```csharp
[Property]
public Property CanUsePositiveAsExchangeRate() =>
    Prop.ForAll(
        Arb.From<Currency>(),
        GetValidRates(),
        ExchangeRateShouldReturnRate);

private static Arbitrary<double> GetValidRates() => Arb.From<double>().MapFilter(_ => _, rate => rate > 0);

private static bool ExchangeRateShouldReturnRate(Currency currency, double rate) =>
    NewExchangeRate.From(currency, rate)
        .Map(value => value.Currency == currency && value.Rate == rate)
        .IfLeft(false);
```

:green_circle: Make it pass.

```csharp
public static Either<Error, NewExchangeRate> From(Currency currency, double rate)
{
    if (rate > 0)
    {
        return Either<Error, NewExchangeRate>.Right(new NewExchangeRate(currency, rate));
    }
    
    return Either<Error, NewExchangeRate>.Left(new Error("Exchange rate should be greater than 0."));
}
```

:large_blue_circle: Refactor.

```csharp
public struct NewExchangeRate
{
    public NewExchangeRate(Currency currency, double rate) { ... }
    public Currency Currency { get; }
    public double Rate { get; }

    public static Either<Error, NewExchangeRate> From(Currency currency, double rate) =>
        IsValidRate(rate)
            ? Either<Error, NewExchangeRate>.Right(new NewExchangeRate(currency, rate))
            : Either<Error, NewExchangeRate>.Left(new Error("Exchange rate should be greater than 0."));

    private static bool IsValidRate(double rate) => rate > 0;
}

public class NewExchangeRateProperties
{
    [Property]
    public Property CanUsePositiveAsExchangeRate() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            GetValidRates(),
            ExchangeRateShouldReturnRate);
}
```

:large_blue_circle: Now that we finalized our implementation, we can use the factory method in the `NewBankProperties`,
and make the `NewExchangeRate` constructor private to enforce parsing.

```csharp
public class NewBankProperties
{
    private static bool AddShouldReturnBankForExchangeRateForDifferentCurrencyThanPivot(Currency pivot,
        Currency currency, double rate) =>
        NewBank
            .WithPivotCurrency(pivot)
            .Add(CreateExchangeRate(currency, rate))
            .IsRight;

    private static bool AddShouldReturnErrorForSameCurrencyAsPivot(Currency currency, double rate, string message) =>
        NewBank
            .WithPivotCurrency(currency)
            .Add(CreateExchangeRate(currency, rate)) == Either<Error, NewBank>.Left(new Error(message));

    private static NewExchangeRate CreateExchangeRate(Currency currency, double rate) =>
        (NewExchangeRate) NewExchangeRate.From(currency, rate).Case;
}

public struct NewExchangeRate
{
    private NewExchangeRate(Currency currency, double rate)
    {
        this.Currency = currency;
        this.Rate = rate;
    }
    
    public static Either<Error, NewExchangeRate> From(Currency currency, double rate) =>
    IsValidRate(rate)
        ? Either<Error, NewExchangeRate>.Right(new NewExchangeRate(currency, rate))
        : Either<Error, NewExchangeRate>.Left(new Error("Exchange rate should be greater than 0."));
}
```

> Update an exchange rate

```text
for all (pivotCurrency, currency, positiveDouble)
such that currency != pivotCurrency
CreateBankWithPivotCurrency(pivotCurrency)
    .Add(new ExchangeRate(positiveDouble, currency))
    .Add(new ExchangeRate(positiveDouble + 0.1, currency)) should return success
```

:green_circle: The bank hides its exchange rates. This behaviour comes for free because we can only test that the return
value is an instance of `NewBank`.
It is **already** implemented.
Hence, we will not go through the red step.

We still want the test to be visible in our living documentation.

```csharp
[Property]
private Property CanUpdateExchangeRateForAnyCurrencyDifferentThanPivot() =>
    Prop.ForAll(
        Arb.From<Currency>(),
        Arb.From<Currency>(),
        GetValidRates(),
        (pivot, currency, rate) => AddShouldReturnBankWhenUpdatingExchangeRate(pivot, currency, rate)
            .When(pivot != currency));

private static bool AddShouldReturnBankWhenUpdatingExchangeRate(Currency pivot, Currency currency, double rate) =>
    NewBank.WithPivotCurrency(pivot)
        .Add(CreateExchangeRate(currency, rate))
        .Map(bank => bank.Add(CreateExchangeRate(currency, rate + 1)))
        .IsRight;
```

We can make it fail by introducing a manual mutant to improve your confidence in this property.

The actual output of `Add` will be tested while testing the `Convert` method by increasing the size of our
system-under-test.

Remember that you should **never** break encapsulation for the sake of testing.

#### Convert a Money
![Convert a Money](img/BankRedesignConvert.png)

> Convert in unknown currencies

```text
for all (pivotCurrency, currency, money)
such that currency != pivotCurrency
CreateBankWithPivotCurrency(pivotCurrency)
    .Convert(money, currency) should return error(money.currency->currency)
```

:red_circle: As usual, let's write a test first.

```csharp
[Property]
private Property CannotConvertToUnknownCurrency() =>
    Prop.ForAll(Arb.From<Currency>(),
        Arb.From<Currency>(),
        MoneyGenerator.GenerateMoneys(),
        (pivot, currency, money) => (NewBank.WithPivotCurrency(pivot).Convert(money, currency) ==
                                    Either<Error, Money>.Left(new Error($"{money.Currency}->{currency}.")))
                                    .When(pivot != currency && money.Currency != currency));

public class NewBank 
{
    public Either<Error, Money> Convert(Money money, Currency currency)
    {
        throw new NotImplementedException();
    }
}
```

:green_circle: Make it pass.
```csharp
public class NewBank 
{
    public Either<Error, Money> Convert(Money money, Currency currency)
    {
        return new Error($"{money.Currency}->{currency}.");
    }
}
```

:large_blue_circle: Refactor and reduce the noise in the test.
````csharp
[Property]
private Property CannotConvertToUnknownCurrency() =>
    Prop.ForAll(Arb.From<Currency>(),
        Arb.From<Currency>(),
        MoneyGenerator.GenerateMoneys(),
        (pivot, currency, money) => ConvertShouldReturnErrorWhenCurrencyIsUnknown(pivot, money, currency)
        .When(pivot != currency && money.Currency != currency));

private static bool ConvertShouldReturnErrorWhenCurrencyIsUnknown(Currency pivot, Money money, Currency currency) =>
    NewBank.WithPivotCurrency(pivot).Convert(money, currency) ==
    Either<Error, Money>.Left(new Error($"{money.Currency}->{currency}."));
````

> Convert from any currency to same currency should return the same amount

We can rationalize this as converting money in the same currency should return the same money, no matter the pivot currency.

```text
for all (pivot, money)
CreateBankWithPivotCurrency(pivot)
    .Convert(money, money.currency) should return money
```

:red_circle: As usual, write the test first.
```csharp
[Property]
private Property ConvertToSameCurrencyReturnSameMoney() =>
    Prop.ForAll(Arb.From<Currency>(),
        MoneyGenerator.GenerateMoneys(),
        (pivot, money) => (NewBank.WithPivotCurrency(pivot).Convert(money, pivot) == money).When(pivot != money.Currency));
```

:green_circle: Make it pass.
```csharp
public class NewBank 
{
    public Either<Error, Money> Convert(Money money, Currency currency)
    {
        if (money.Currency == currency)
        {
            return Either<Error, Money>.Right(money);
        }
        return Either<Error, Money>.Left(new Error($"{money.Currency}->{currency}."));
    }
}
```

:large_blue_circle: Refactor, reduce noise and make the code more declarative.
```csharp
public class NewBank 
{
    public Either<Error, Money> Convert(Money money, Currency currency) =>
        CanConvertMoney(money, currency)
            ? Either<Error, Money>.Right(money)
            : Either<Error, Money>.Left(new Error(FormatMissingExchangeRate(money.Currency, currency)));
    
    private static bool CanConvertMoney(Money money, Currency currency) => money.Currency == currency;
    
    private static string FormatMissingExchangeRate(Currency from, Currency to) => $"{from}->{to}.";
}

public class NewBankProperties
{
    [Property]
    private Property ConvertToSameCurrencyReturnSameMoney() =>
        Prop.ForAll(
            Arb.From<Currency>(),
            MoneyGenerator.GenerateMoneys(),
            (pivot, money) => ConvertShouldReturnMoneyWhenConvertingToSameCurrency(pivot, money)
                .When(pivot != money.Currency));

    private static bool ConvertShouldReturnMoneyWhenConvertingToSameCurrency(Currency pivot, Money money) =>
        NewBank.WithPivotCurrency(pivot).Convert(money, money.Currency) == money;
}
```

> Convert from pivot currency to another known currency

:red_circle: We want to assess more than just the behaviour but also the values. We can use standard `unit tests` for that.

```csharp
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
}
```

:green_circle: To make it pass, we will finally have to use our internal exchange rates. This test will cover what we previously left out.
```csharp
public class NewBank 
{
    private readonly Seq<NewExchangeRate> exchangeRates;
    private readonly Currency pivotCurrency;

    private NewBank(Currency pivotCurrency, Seq<NewExchangeRate> exchangeRates)
    {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    public static NewBank WithPivotCurrency(Currency currency) => new(currency, Seq<NewExchangeRate>.Empty);

    public Either<Error, NewBank> Add(NewExchangeRate exchangeRate) =>
        this.IsPivotCurrency(exchangeRate.Currency)
            ? Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency."))
            : Either<Error, NewBank>.Right(new NewBank(this.pivotCurrency, this.exchangeRates.Add(exchangeRate)));

    private bool IsPivotCurrency(Currency currency) => currency == this.pivotCurrency;

    public Either<Error, Money> Convert(Money money, Currency currency) =>
        this.CanConvertMoney(money, currency)
            ? this.ConvertMoney(money, currency)
            : Either<Error, Money>.Left(new Error(FormatMissingExchangeRate(money.Currency, currency)));

    private Either<Error, Money> ConvertMoney(Money money, Currency currency)
    {
        if (money.Currency == currency)
        {
            return Either<Error, Money>.Right(money);
        }

        var exchange = this.exchangeRates
            .Find(exchangeRate => exchangeRate.Currency == currency)
            .IfNone(() => throw new NotImplementedException());
        return new Money(money.Amount * exchange.Rate, currency);
    }

    private bool CanConvertMoney(Money money, Currency currency) => money.Currency == currency ||
                                                                    this.exchangeRates.Any(exchange =>
                                                                        exchange.Currency == currency);
}
```

:large_blue_circle: We can now work on code readability and clarity.
```csharp
public class NewBank
{
    public const string SameExchangeRateThanCurrency = "Cannot add an exchange rate for the pivot currency.";
    private readonly Seq<NewExchangeRate> exchangeRates;
    private readonly Currency pivotCurrency;

    private NewBank(Currency pivotCurrency, Seq<NewExchangeRate> exchangeRates)
    {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    public static NewBank WithPivotCurrency(Currency currency) => new(currency, LanguageExt.Seq<NewExchangeRate>.Empty);

    public Either<Error, NewBank> Add(NewExchangeRate exchangeRate) =>
        this.IsPivotCurrency(exchangeRate.Currency)
            ? Either<Error, NewBank>.Left(new Error(SameExchangeRateThanCurrency))
            : Either<Error, NewBank>.Right(this.AddExchangeRate(exchangeRate));

    private NewBank AddExchangeRate(NewExchangeRate exchangeRate) =>
        new(this.pivotCurrency, this.exchangeRates
            .Add(exchangeRate));

    private bool IsPivotCurrency(Currency currency) => currency == this.pivotCurrency;

    public Either<Error, Money> Convert(Money money, Currency currency) =>
        this.GetExchangeRate(money, currency)
            .Map(rate => ConvertUsingExchangeRate(money, rate))
            .Match(some => Either<Error, Money>.Right(some),
                () => Either<Error, Money>.Left(new Error(FormatMissingExchangeRate(money.Currency, currency))));

    private Option<NewExchangeRate> GetExchangeRate(Money money, Currency currency) =>
        money.HasCurrency(currency)
            ? NewExchangeRate
                .From(currency, 1)
                .Match(Some, _ => Option<NewExchangeRate>.None)
            : this.FindExchangeRate(currency);

    private static Money ConvertUsingExchangeRate(Money money, NewExchangeRate exchangeRate) =>
        new(money.Amount * exchangeRate.Rate, exchangeRate.Currency);

    private Option<NewExchangeRate> FindExchangeRate(Currency currency) =>
        this.exchangeRates.Find(exchange => exchange.Currency == currency);

    private static string FormatMissingExchangeRate(Currency from, Currency to) => $"{from}->{to}.";
}
```

:red_circle: We need to be cautious about a scenario here that we faced before with the previous bank implementation:
When updating an exchange rate, we add another element to the list.
It means the previous one is still on the list, and `.Find` will likely pick the old one.

Let's write a test to check this one out.

:red_circle: And it's a red step because it **fails**.
```csharp
[Fact]
public void ConvertInDollarsFromEurosWithUpdatedRate() =>
    NewBank
        .WithPivotCurrency(Currency.EUR)
        .Add(DomainUtility.CreateExchangeRate(Currency.USD, 1.1))
        .Bind(bank => bank.Add(DomainUtility.CreateExchangeRate(Currency.USD, 1.2)))
        .Map(bank => bank.Convert(10d.Euros(), Currency.USD))
        .Should()
        .Be(12d.Dollars());
```

:green_circle: Make it pass.
```csharp
private NewBank AddExchangeRate(NewExchangeRate exchangeRate) =>
    new(this.pivotCurrency, this.exchangeRates
        .Filter(element => element.Currency != exchangeRate.Currency)
        .Add(exchangeRate));
```

> Convert through Pivot Currency

Let's add the last example: convert through pivot currency.
```gherkin
Given a bank with a EUR currency
And has an exchange rate of 1.2 for USD 
And has an exchange rate of 1344 for KRX
When I convert 10 USD to KRW
Then the bank should return 11200 KRW
```

:red_circle: Write a failing test with our example.
```csharp
[Fact]
public void ConvertThroughPivotCurrency() =>
    NewBank
        .WithPivotCurrency(Currency.EUR)
        .Add(DomainUtility.CreateExchangeRate(Currency.USD, 1.2))
        .Bind(bank => bank.Add(DomainUtility.CreateExchangeRate(Currency.KRW, 1344)))
        .Map(bank => bank.Convert(10d.Dollars(), Currency.KRW))
        .Should()
        .Be(11200d.KoreanWons());
```

:green_circle: We need to handle how to convert through the pivot currency: source -> pivot -> destination.
There are multiple ways to take that.
We could consider a single operation while creating a `computed` exchange rate.
It would limit the impact on the code.

```csharp
public class NewBank 
{
    private Option<NewExchangeRate> GetExchangeRate(Money money, Currency currency)
    {
        if (money.HasCurrency(currency))
        {
            return NewExchangeRate
                .From(currency, 1)
                .Match(Some, _ => Option<NewExchangeRate>.None);
        }

        var exchangeSource = this.FindExchangeRate(money.Currency);
        var exchangeDestination = this.FindExchangeRate(currency);
        return !this.IsPivotCurrency(money.Currency)
               && !this.IsPivotCurrency(currency)
            ? ComputeExchangeRate(exchangeSource, exchangeDestination, currency)
            : exchangeDestination;
    }

    private static Option<NewExchangeRate> ComputeExchangeRate(Option<NewExchangeRate> source, Option<NewExchangeRate> destination)
    {
        return destination
            .Bind(exchange => source
                .Map(sourceExchange => 1 / sourceExchange.Rate)
                .Map(rate => rate * exchange.Rate)
                .Map(rate => new Tuple<Currency, double>(exchange.Currency, rate)))
            .Map(tuple => NewExchangeRate
                .From(tuple.Item1, tuple.Item2)
                .IfLeft(_ => NewExchangeRate.Default(tuple.Item1)));
    }
}
```

:large_blue_circle: Simplify the code and its readability.

```csharp
public class NewBank 
{
        private Option<NewExchangeRate> GetExchangeRate(Money money, Currency currency)
    {
        if (money.HasCurrency(currency))
        {
            return GetExchangeRateForSameCurrency(currency);
        }

        var exchangeSource = this.FindExchangeRate(money.Currency);
        var exchangeDestination = this.FindExchangeRate(currency);
        return this.ShouldConvertThroughPivotCurrency(money.Currency, currency)
            ? ComputeExchangeRate(exchangeSource, exchangeDestination)
            : exchangeDestination;
    }

    private static Option<NewExchangeRate> GetExchangeRateForSameCurrency(Currency currency) =>
        NewExchangeRate
            .From(currency, 1)
            .Match(Some, _ => Option<NewExchangeRate>.None);

    private bool ShouldConvertThroughPivotCurrency(Currency source, Currency destination) =>
        !this.IsPivotCurrency(source) && !this.IsPivotCurrency(destination);

    private static Option<NewExchangeRate> ComputeExchangeRate(Option<NewExchangeRate> source, Option<NewExchangeRate> destination) =>
        destination
            .Bind(exchange => source
                .Map(sourceExchange => GetReversedRate(sourceExchange.Rate))
                .Map(rate => rate * exchange.Rate)
                .Map(rate => new Tuple<Currency, double>(exchange.Currency, rate)))
            .Map(exchange => NewExchangeRate
                .From(exchange.Item1, exchange.Item2)
                .IfLeft(_ => NewExchangeRate.Default(exchange.Item1)));
}
```

:large_blue_circle: Simplify monad typing using implicit conversions.

From this:

```csharp
public Either<Error, NewBank> Add(NewExchangeRate exchangeRate) =>
    this.IsPivotCurrency(exchangeRate.Currency)
        ? Either<Error, NewBank>.Left(new Error(SameExchangeRateThanCurrency))
        : Either<Error, NewBank>.Right(this.AddExchangeRate(exchangeRate));
        
public Either<Error, Money> Convert(Money money, Currency currency) =>
    this.GetExchangeRate(money, currency)
        .Map(rate => ConvertUsingExchangeRate(money, rate))
        .Match(some => Either<Error, Money>.Right(some),
            () => Either<Error, Money>.Left(new Error(FormatMissingExchangeRate(money.Currency, currency))));
```

To this:

```csharp
public Either<Error, NewBank> Add(NewExchangeRate exchangeRate) =>
    this.IsPivotCurrency(exchangeRate.Currency)
        ? new Error(SameExchangeRateThanCurrency)
        : this.AddExchangeRate(exchangeRate);
        
public Either<Error, Money> Convert(Money money, Currency currency) =>
    this.GetExchangeRate(money, currency)
        .Map(rate => ConvertUsingExchangeRate(money, rate))
        .Match(Either<Error, Money>.Right,
            () => new Error(FormatMissingExchangeRate(money.Currency, currency)));
```

### Parameterized Tests
Are we confident enough with those `properties` and `unit tests`?

We may add some other examples to increase our confidence.
We can use `parameterized tests` to make it easiest to use different examples for the same behavior. 
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
```

Start by changing an existing test to parameterized test:
```java
    private static Stream<Arguments> examplesForConvertingThroughPivotCurrency() {
        return Stream.of(
                Arguments.of(dollars(10), KRW, koreanWons(11200))
        );
    }

    @ParameterizedTest
    @MethodSource("examplesForConvertingThroughPivotCurrency")
    void convertThroughPivotCurrency(Money money, Currency to, Money expectedResult) {
        assertThat(bank.add(rateFor(1.2, USD))
                .flatMap(b -> b.add(rateFor(1344, KRW)))
                .flatMap(newBank -> newBank.convert(money, to)))
                .containsOnRight(expectedResult);
    }
```

We use `@MethodSource` as source for our test. Observe your `Test Result`, it is pretty readable right 👌
![Output of parameterized tests](img/bank-redesign-example.png)

Parameterized tests are really ideal to test pure functions.

> Avoid the trap of putting conditions in this kind of tests. If at one point your tempted to do so, it means you have 2 test cases, so split it.

Add other examples:
```java
private static Stream<Arguments> examplesForConvertingThroughPivotCurrency() {
    return Stream.of(
            Arguments.of(dollars(10), KRW, koreanWons(11200)),
            Arguments.of(dollars(-1), KRW, koreanWons(-1120)),
            Arguments.of(koreanWons(39_345.50), USD, dollars(35.129910714285714)),
            Arguments.of(koreanWons(1000), USD, dollars(0.8928571428571427))
    );
}
```
:large_blue_circle: Centralize conversion tests in parameterized tests:
```java
class NewBankTest {
    public static final Currency PIVOT_CURRENCY = EUR;
    private final Bank bank = Bank.withPivotCurrency(PIVOT_CURRENCY);

    private static Stream<Arguments> examplesOfDirectConversion() {
        return Stream.of(
                of(dollars(87), EUR, euros(72.5)),
                of(koreanWons(1_009_765), EUR, euros(751.313244047619)),
                of(euros(10), USD, dollars(12)),
                of(euros(543.98), USD, dollars(652.776))
        );
    }

    @ParameterizedTest
    @MethodSource("examplesOfDirectConversion")
    void convertDirectly(Money money, Currency to, Money expectedResult) {
        assertConversion(money, to, expectedResult);
    }

    private static Stream<Arguments> examplesOfConversionThroughPivotCurrency() {
        return Stream.of(
                of(dollars(10), KRW, koreanWons(11200)),
                of(dollars(-1), KRW, koreanWons(-1120)),
                of(koreanWons(39_345.50), USD, dollars(35.129910714285714)),
                of(koreanWons(1000), USD, dollars(0.8928571428571427))
        );
    }

    @ParameterizedTest
    @MethodSource("examplesOfConversionThroughPivotCurrency")
    void convertThroughPivotCurrency(Money money, Currency to, Money expectedResult) {
        assertConversion(money, to, expectedResult);
    }

    private void assertConversion(Money money, Currency to, Money expectedResult) {
        assertThat(bank.add(rateFor(1.2, USD))
                .flatMap(b -> b.add(rateFor(1344, KRW)))
                .flatMap(newBank -> newBank.convert(money, to)))
                .containsOnRight(expectedResult);
    }
}
```

> We have chosen to keep the separation between direct conversion and conversion through pivot currency to make it clear when a test fails why it fails.

### Strangler on the `Portfolio`
Now that we have defined our new bank implementation using T.D.D outside from the current production code we can intercept and refactor the `Portfolio`.
Let's use another `Strangler`

:red_circle: We start by a failing test / a new expectation
```java
class PortfolioTest {
    private Bank bank;
    private NewBank newBank;

    @BeforeEach
    void setup() {
        bank = Bank.withExchangeRate(EUR, USD, 1.2)
                .addExchangeRate(USD, KRW, 1100);

        newBank = NewBank.withPivotCurrency(EUR)
                .add(rateFor(1.2, USD))
                .flatMap(n -> n.add(rateFor(1344, KRW)))
                .get();
    }

    @Test
    @DisplayName("5 USD + 10 USD = 15 USD")
    void shouldAddMoneyInTheSameCurrency() {
        var portfolio = portfolioWith(
                dollars(5),
                dollars(10)
        );

        assertThat(portfolio.evaluate(newBank, USD))
                .containsOnRight(dollars(15));
    }
    ...
}
```

Generate the new `evaluate` method:
```java
public Either<Error, Money> evaluate(NewBank bank, Currency to) {
    return null;
}
```

:green_circle: We can duplicate internal methods to make it pass.
Here we don't manipulate the same types, so it is easier to do it that way.

```java
public final class Portfolio {
    private final Seq<Money> moneys;

    public Portfolio() {
        this.moneys = Vector.empty();
    }

    private Portfolio(Seq<Money> moneys) {
        this.moneys = moneys;
    }

    public Portfolio add(Money money) {
        return new Portfolio(moneys.append(money));
    }

    public Either<String, Money> evaluate(Bank bank, Currency toCurrency) {
        var convertedMoneys = convertAllMoneys(bank, toCurrency);

        return this.containsFailureOld(convertedMoneys)
                ? left(toFailureOld(convertedMoneys))
                : right(sumConvertedMoneyOld(convertedMoneys, toCurrency));
    }

    private Seq<Either<String, Money>> convertAllMoneys(Bank bank, Currency toCurrency) {
        return moneys.map(money -> bank.convert(money, toCurrency));
    }

    private boolean containsFailureOld(Seq<Either<String, Money>> convertedMoneys) {
        return convertedMoneys.exists(Either::isLeft);
    }

    private String toFailureOld(Seq<Either<String, Money>> convertedMoneys) {
        return convertedMoneys
                .filter(Either::isLeft)
                .map(e -> String.format("[%s]", e.getLeft()))
                .mkString("Missing exchange rate(s): ", ",", "");
    }

    private Money sumConvertedMoneyOld(Seq<Either<String, Money>> convertedMoneys, Currency toCurrency) {
        return new Money(convertedMoneys
                .filter(Either::isRight)
                .map(e -> e.getOrElse(new Money(0, toCurrency)))
                .map(Money::amount)
                .reduce(Double::sum), toCurrency);
    }

    private Seq<Either<Error, Money>> convertAllMoneys(NewBank bank, Currency toCurrency) {
        return moneys.map(money -> bank.convert(money, toCurrency));
    }

    private boolean containsFailure(Seq<Either<Error, Money>> convertedMoneys) {
        return convertedMoneys.exists(Either::isLeft);
    }

    private Error toFailure(Seq<Either<Error, Money>> convertedMoneys) {
        return new Error(convertedMoneys
                .filter(Either::isLeft)
                .map(e -> String.format("[%s]", e.getLeft().message()))
                .mkString("Missing exchange rate(s): ", ",", ""));
    }

    private Money sumConvertedMoney(Seq<Either<Error, Money>> convertedMoneys, Currency toCurrency) {
        return new Money(convertedMoneys
                .filter(Either::isRight)
                .map(e -> e.getOrElse(new Money(0, toCurrency)))
                .map(Money::amount)
                .reduce(Double::sum), toCurrency);
    }

    public Either<Error, Money> evaluate(NewBank bank, Currency to) {
        var convertedMoneys = convertAllMoneys(bank, to);

        return containsFailure(convertedMoneys)
                ? left(toFailure(convertedMoneys))
                : right(sumConvertedMoney(convertedMoneys, to));
    }
}
```

Continue by calling the new `evaluate` method in each test:
```java
    @Test
    @DisplayName("5 USD + 10 USD = 15 USD")
    void shouldAddMoneyInTheSameCurrency() {
        var portfolio = portfolioWith(
                dollars(5),
                dollars(10)
        );

        assertThat(portfolio.evaluate(newBank, USD))
                .containsOnRight(dollars(15));
    }

    @Test
    @DisplayName("5 USD + 10 EUR = 17 USD")
    void shouldAddMoneyInDollarsAndEuros() {
        var portfolio = portfolioWith(
                dollars(5),
                euros(10)
        );

        assertThat(portfolio.evaluate(newBank, USD))
                .containsOnRight(dollars(17));
    }

    @Test
    @DisplayName("1 USD + 1100 KRW = 2200 KRW")
    void shouldAddMoneyInDollarsAndKoreanWons() {
        var portfolio = portfolioWith(
                dollars(1),
                koreanWons(1100)
        );

        assertThat(portfolio.evaluate(newBank, KRW))
                .containsOnRight(koreanWons(2200));
    }

    @Test
    @DisplayName("5 USD + 10 EUR + 4 EUR = 21.8 USD")
    void shouldAddMoneyInDollarsAndMultipleAmountInEuros() {
        var portfolio = portfolioWith(
                dollars(5),
                euros(10),
                euros(4)
        );

        assertThat(portfolio.evaluate(newBank, USD))
                .containsOnRight(dollars(21.8));
    }
```

:red_circle: We detect a problem with one of the assertion:
```java
    @Test
    @DisplayName("1 USD + 1100 KRW = 2200 KRW")
    void shouldAddMoneyInDollarsAndKoreanWons() {
        var portfolio = portfolioWith(
                dollars(1),
                koreanWons(1100)
        );

        assertThat(portfolio.evaluate(newBank, KRW))
                .containsOnRight(koreanWons(2200));
    }
```

:green_circle: The previous `Bank` implementation were not that good and with our new knowledge we can fix the assertion.
```java
 @Test
 @DisplayName("1 USD + 1100 KRW = 2220 KRW")
 void shouldAddMoneyInDollarsAndKoreanWons() {
     var portfolio = portfolioWith(
             dollars(1),
             koreanWons(1100)
     );

     assertThat(portfolio.evaluate(newBank, KRW))
             .containsOnRight(koreanWons(2220));
 }
```

:red_circle: Let's work on the last test case.
This one is more interesting, since we use the concept of `Pivot Currency` this test case is no longer valid...
To make it valid we need to use an `empty` instance of a `Bank`.
```java
@Test
@DisplayName("Return a failure result in case of missing exchange rates")
void shouldReturnAFailingResultInCaseOfMissingExchangeRates() {
    var portfolio = portfolioWith(
            euros(1),
            dollars(1),
            koreanWons(1)
    );

    assertThat(portfolio.evaluate(newBank, EUR))
            .containsOnLeft(error("Missing exchange rate(s): [USD->EUR],[KRW->EUR]"));
}
```

Let's adapt the test:
```java
    @Test
    @DisplayName("Return a failure result in case of missing exchange rates")
    void shouldReturnAFailingResultInCaseOfMissingExchangeRates() {
        var portfolio = portfolioWith(
                euros(1),
                dollars(1),
                koreanWons(1)
        );

        var emptyBank = withPivotCurrency(EUR);

        assertThat(portfolio.evaluate(emptyBank, EUR))
                .containsOnLeft(error("Missing exchange rate(s): [USD->EUR],[KRW->EUR]"));
    }
```

We detect a mistake in our `Bank` implementation, the errors are structured differently from the previous implementation:
![Failure in missing rates](img/bank-redesign-missing-rates.png)

:green_circle: Change the `Bank` error instantiation:
```java
    public Either<Error, Money> convert(Money money, Currency to) {
        return convert.find(canConvert -> canConvert._1.apply(money, to))
                .map(k -> k._2.apply(money, to))
                .toEither(new Error(keyFor(money.currency(), to)));
    }
```

:large_blue_circle: We can now clean our code:
- Remove the former `evaluate` method and its related ones
- Clean the `Portfolio` test to remove the `Bank` concept

![Safe delete](img/bank-redesign-safe-delete.png)

- Delete the former `Bank` implementation
  - Its associated properties and tests
- Rename `NewBank` to simply `Bank`
  - Your IDE will rename tests for you 

![Rename Bank](img/bank-redesign-rename.png)

### Reflect
In this step we have used a lot of concepts discovered in previous steps:
- `Sprout Technique` to create our `Bank` implementation without impacting existing code
- `Example mapping` outcome to design our tests and properties
- `Property-Based Testing` as a driver for T.D.D
- `Parameterized tests` to assert the behaviours of a pure function
- `Strangler pattern` to finalize the `Sprout` and remove the former `Bank` implementation
- `Fight primitive obsession` by:
  - introducing an object for `Error` make it more clear our method signatures: `Bank` -> `Currency` -> `Either<Error, Money>`
  - encapsulating rate business rules inside the `ExchangeRate` class

![What a journey](../../docs/img/bank-redesign.png)

What a journey so far...
