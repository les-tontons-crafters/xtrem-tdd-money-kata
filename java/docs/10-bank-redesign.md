## Redesign the Bank
Based on our new understanding / discoveries from the `example mapping` workshop we can work on the redesign of the `Bank`.
The examples will serve to drive our implementation.

If we increment on the existing `Bank` code, we will have directly huge impact on the whole system.

### Sprout Technique
One way to avoid it is to use a technique called [Sprout Technique](https://understandlegacycode.com/blog/key-points-of-working-effectively-with-legacy-code/#1-the-sprout-technique) from [Michael Feathers](https://wiki.c2.com/?MichaelFeathers) in his book [Working Effectively with Legacy Code](https://www.oreilly.com/library/view/working-effectively-with/0131177052/):
- Create your code somewhere else (use T.D.D to drive your implementation)
- Identify where you should call that code from the existing code: the insertion point.
- Call your code from the Existing or Legacy Code.

> Let's use this technique

### Tests list
Based on the example mapping outcome we can create a test list and think about the design of our `Bank`.

#### Define pivot currency
![Define Pivot Currency](img/bank-redesign-pivot-currency.png)

Regarding our business, those rules are really important and should be at the heart of our system.
To implement those, we can simply well encapsulate our class by making impossible `by design` to change the pivot currency of a living `Bank` instance.

We have not really test cases here, but it gives us ideas for preserving the integrity of our system.

#### Add an exchange rate
![Add an exchange rate](img/bank-redesign-add-echange-rate.png)

We can use `Property-Based Testing` here to check those rules

Can not add an exchange rate for the pivot currency of the `Bank`:
```text
for all (currency, positiveDouble)
createBankWithPivotCurrency(currency)
    .add(new ExchangeRate(positiveDouble, currency)) should return an error("Can not add an exchange rate for the pivot currency")
```

Can not use a negative double or 0 as exchange rate: 
```text
for all (pivotCurrency, currency, negativeOr0Double)
such that currency != pivotCurrency
createBankWithPivotCurrency(pivotCurrency)
    .add(new ExchangeRate(negativeOr0Double, currency)) should return error("Exchange rate should be greater than 0")
```

Add an exchange rate for a Currency:
```text
for all (pivotCurrency, currency, positiveDouble)
such that currency != pivotCurrency
createBankWithPivotCurrency(pivotCurrency)
    .add(new ExchangeRate(positiveDouble, currency)) should return success
```

Let's start to work on those properties first.
:red_circle: As usual, we start by a failing test / property on a new bank implementation

```java
@RunWith(JUnitQuickcheck.class)
public class NewBankProperties {
    @Property
    public void canNotAddAnExchangeRateForThePivotCurrencyOfTheBank(Currency pivotCurrency, @InRange(min = "0.000001", max = "100_000") double validAmount) {
        assertThat(NewBank.withPivotCurrency(pivotCurrency)
                .add(new ExchangeRate(validAmount, pivotCurrency))
                .containsOnLeft(new money_problem.domain.Error("Can not add an exchange rate for the pivot currency"));
    }
}
```

We have some code to generate from here:
![canNotAddAnExchangeRateForThePivotCurrencyOfTheBank](img/bank-redesign-canNotAddAnExchangeRateForThePivotCurrencyOfTheBank.png)

```java
public class NewBank {
    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return null;
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return null;
    }
}

public record Error(String message) {
}

public record ExchangeRate(double amount, Currency currency) {
}
```

:green_circle: We can then fake the expected behavior to make it green.
```java
public class NewBank {
    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank();
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return Left(new Error("Can not add an exchange rate for the pivot currency"));
    }
}
```

:large_blue_circle: Any improvement?

:red_circle: Let's add a second property


#### Convert a Money
![Convert a Money](img/bank-redesign-convert.png)

Convert from pivot to pivot currency:
```text
for all (pivotCurrency, amount)
createBankWithPivotCurrency(pivotCurrency)
    .convert(amount, pivotCurrency) should return amount
```


:red_circle: add a failing test on a new bank implementation



- From examples: edge cases
- ConvertThroughPivotCurrency
- PBT removed after we learned a lot ?
   - Check which property makes still sense
- [Parse don't validate](https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/)
- Continuation functions instead of procedural code
- Test improvement 
    - Parameterized Tests
    - Higher order function
    - Organize tests: failure vs success -> Nested classes
- Public contract of the bank:
    - add(ExchangeRate rate) -> ExchangeRate Currency + double
    - convert(Money money, Currency currency) -> Either<Error, Bank>
- Use Error types instead of String when using Either
- Call the branch -> 10-bank-redesign