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

Let's start to work on this property first.

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

We have some code to generate from here, you remember we wanted to fight primitive obsession so decided to represent the `ExchangeRate` as a business concept.
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

Can not use a negative double or 0 as exchange rate: 
```text
for all (pivotCurrency, currency, negativeOr0Double)
such that currency != pivotCurrency
createBankWithPivotCurrency(pivotCurrency)
    .add(new ExchangeRate(negativeOr0Double, currency)) should return error("Exchange rate should be greater than 0")
```

:red_circle: Let's add a second property regarding the fact an `Exchange rate` should never be negative or equal to 0.
It should be a responsibility of the `EchangeRate` data structure so let's write this property aside from the ones of the `Bank`:
```java
@RunWith(JUnitQuickcheck.class)
public class ExchangeRateProperties {
    @Property
    public void canNotUseANegativeDoubleOrZeroAsExchangeRate(
            @InRange(max = "0") double invalidAmount,
            Currency anyCurrency) {
        assertThat(ExchangeRate.from(invalidAmount, anyCurrency))
                .containsOnLeft(new Error("Exchange rate should be greater than 0"));
    }
}
```

:green_circle: Create the method and fake its behavior for now.
Here to preserve encapsulation and force the usage of the `from` factory method, we choose to use a classic class and not a record.

You can see the `from` method as a parsing one. Here we apply a principle called [`parse don't validate`](https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/). 
> Once an object is instantiated, we know for sure that it is valid.

If you use only primitive types, this property is hard to achieve and you will have to make a lot of validation in different places inside your system. 

```java
public class ExchangeRate {
    private double rate;
    private Currency currency;

    private ExchangeRate(double rate, Currency currency) {
        this.rate = rate;
        this.currency = currency;
    }

    public static Either<Error, ExchangeRate> from(double rate, Currency currency) {
        return Left(new Error("Exchange rate should be greater than 0"));
    }
}
```

:large_blue_circle: Improve our exchange rate instantiation in test
```java
@RunWith(JUnitQuickcheck.class)
public class NewBankProperties {
    @Property
    public void canNotAddAnExchangeRateForThePivotCurrencyOfTheBank(Currency pivotCurrency, @InRange(min = "0.000001", max = "100000") double validRate) {
        assertThat(withPivotCurrency(pivotCurrency)
                .add(createExchangeRate(pivotCurrency, validRate)))
                .containsOnLeft(new Error("Can not add an exchange rate for the pivot currency"));
    }

    private ExchangeRate createExchangeRate(Currency pivotCurrency, double validRate) {
        return from(validRate, pivotCurrency).get();
    }
}
```


Add an exchange rate for a Currency:
```text
for all (pivotCurrency, currency, positiveDouble)
such that currency != pivotCurrency
createBankWithPivotCurrency(pivotCurrency)
    .add(new ExchangeRate(positiveDouble, currency)) should return success
```

:red_circle: Let's triangulate the success `add` implementation now
```java
@Property
public void canAddAnExchangeRateForAnyCurrencyDifferentFromThePivot(
        Currency pivotCurrency,
        Currency otherCurrency,
        @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
    assumeTrue(pivotCurrency != otherCurrency);

    assertThat(withPivotCurrency(pivotCurrency)
            .add(createExchangeRate(otherCurrency, validRate)))
            .isRight();
}
```

:green_circle: Improve the `ExchangeRate` design to move on.
```java
public class ExchangeRate {
    private double rate;
    private Currency currency;

    private ExchangeRate(double rate, Currency currency) {
        this.rate = rate;
        this.currency = currency;
    }

    private static boolean isPositive(double rate) {
        return rate > 0;
    }

    public static Either<Error, ExchangeRate> from(double rate, Currency currency) {
        return isPositive(rate)
                ? Right(new ExchangeRate(rate, currency))
                : Left(new Error("Exchange rate should be greater than 0"));
    }
}
```

We then need to work at the `Bank` level:
```java
public class NewBank {
    private Currency pivotCurrency;

    private NewBank(Currency pivotCurrency) {
        this.pivotCurrency = pivotCurrency;
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        if (exchangeRate.getCurrency() == pivotCurrency) {
            return Left(new Error("Can not add an exchange rate for the pivot currency"));
        }
        return Right(new NewBank(pivotCurrency));
    }
}

And expose the currency of the ExchangeRate

public class ExchangeRate {
    ...
    public Currency getCurrency() {
        return currency;
    }
    ...
}
```

:large_blue_circle: We can improve our `Bank` implementation:

```java
public class NewBank {
    private final Currency pivotCurrency;

    private NewBank(Currency pivotCurrency) {
        this.pivotCurrency = pivotCurrency;
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return exchangeRate.getCurrency() != pivotCurrency
                ? Right(new NewBank(pivotCurrency))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }
}
```

update an exchange rate for a Currency:
```text
for all (pivotCurrency, currency, positiveDouble)
such that currency != pivotCurrency
createBankWithPivotCurrency(pivotCurrency)
    .add(new ExchangeRate(positiveDouble, currency))
    .add(new ExchangeRate(positiveDouble + 0.1, currency)) should return success
```

:green_circle: This behavior comes for free with our current design.
```java
@Property
public void canUpdateAnExchangeRateForAnyCurrencyDifferentFromThePivot(
        Currency pivotCurrency,
        Currency otherCurrency,
        @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
    notPivotCurrency(pivotCurrency, otherCurrency);

    var exchangeRate = createExchangeRate(otherCurrency, validRate);
    var updatedExchangeRate = createExchangeRate(otherCurrency, validRate + 0.1);

    assertThat(withPivotCurrency(pivotCurrency)
            .add(exchangeRate)
            .flatMap(newBank -> newBank.add(updatedExchangeRate)))
            .isRight();
}
```

Make it fail by introducing a manual mutant to improve your confidence into this property.

#### Convert a Money
![Convert a Money](img/bank-redesign-convert.png)

Convert in unknown currencies:
```text
for all (pivotCurrency, currency, money)
such that currency != pivotCurrency
createBankWithPivotCurrency(pivotCurrency)
    .convert(money, currency) should return error(money.currency->currency)
```

:red_circle: Add this failing test
```java
@Property
public void canNotConvertToAnUnknownCurrencies(
        Currency pivotCurrency,
        Currency otherCurrency,
        @From(MoneyGenerator.class) Money money) {
    notPivotCurrency(pivotCurrency, otherCurrency);

    assertThat(withPivotCurrency(pivotCurrency)
            .convert(money, otherCurrency))
            .containsOnLeft(new Error("No exchange rate defined for " + money.currency() + "->" + otherCurrency));
}

public class NewBank {
    private final Currency pivotCurrency;

    private NewBank(Currency pivotCurrency) {
        this.pivotCurrency = pivotCurrency;
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return exchangeRate.getCurrency() != pivotCurrency
                ? Right(new NewBank(pivotCurrency))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    // To compile
    public Either<Error, NewBank> convert(Money money, Currency to) {
        return null;
    }
}
```

:green_circle: Make it pass.
```java
public class NewBank {
    ...

    public Either<Error, Money> convert(Money money, Currency to) {
        return Left(new Error("No exchange rate defined for " + money.currency() + "->" + to));
    }
}
```

:large_blue_circle: Any refactoring?

Let's work on another property: convert any amount from pivot to pivot returns the original money
```text
for all (money)
createBankWithPivotCurrency(money.currency)
    .convert(money, money.currency) should return money
```

:red_circle: Let's describe it with `quickcheck`
```java
@Property
public void convertAnyMoneyInPivotCurrencyToPivotCurrencyReturnMoneyItself(
        @From(MoneyGenerator.class) Money money) {
    assertThat(withPivotCurrency(money.currency())
            .convert(money, money.currency()))
            .containsOnRight(money);
}
```

:green_circle: Improve the `Bank` implementation
```java
public class NewBank {
    ...

    public Either<Error, Money> convert(Money money, Currency to) {
        if (money.currency() == to && to == pivotCurrency) {
            return Right(money);
        }
        return Left(new Error("No exchange rate defined for " + money.currency() + "->" + to));
    }
}
```

:large_blue_circle: Simplify conditions to make it clear what's going on:
```java
public class NewBank {
    ...
    public Either<Error, Money> convert(Money money, Currency to) {
        return convertFromAndToPivotCurrency(money, to)
                ? Right(money)
                : Left(new Error("No exchange rate defined for " + money.currency() + "->" + to));
    }

    private boolean convertFromAndToPivotCurrency(Money money, Currency to) {
        return money.currency() == to && to == pivotCurrency;
    }
}
```

Let's move on with a new rule / examples

```text
for all (pivotCurrency, currency, validRate, money)
such that currency != pivotCurrency
createBankWithPivotCurrency(pivotCurrency)
    .add(new ExchangeRate(validRate, money.currency))
    .convert(money, currency) should return money
```

:red_circle: Let's automate this property
```java
@Property
public void convertAnyMoneyToMoneyCurrencyReturnMoneyItself(
        Currency pivotCurrency,
        @From(MoneyGenerator.class) Money money,
        @InRange(min = MINIMUM_RATE, max = MAXIMUM_RATE) double validRate) {
    notPivotCurrency(pivotCurrency, money.currency());

    assertThat(withPivotCurrency(pivotCurrency)
            .add(createExchangeRate(validRate, money.currency()))
            .flatMap(newBank -> newBank.convert(money, money.currency())))
            .containsOnRight(money);
}
```

:green_circle: Pretty easy to make it pass, we just have to simplify checks in `Bank`
```java
public class NewBank {
    private final Currency pivotCurrency;

    private NewBank(Currency pivotCurrency) {
        this.pivotCurrency = pivotCurrency;
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return exchangeRate.getCurrency() != pivotCurrency
                ? Right(new NewBank(pivotCurrency))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    public Either<Error, Money> convert(Money money, Currency to) {
        return canConvert(money, to)
                ? Right(money)
                : Left(new Error("No exchange rate defined for " + money.currency() + "->" + to));
    }

    private boolean canConvert(Money money, Currency to) {
        return money.currency() == to;
    }
}
```

:large_blue_circle: Not that much to refactor right now.

Let's move on another test case: "convert from pivot currency to another known currency".
Here we would like to assess more than just the behavior but also the values. We can use normal `unit tests` for that.

:red_circle: Convert from pivot to an existing currency

```java
class NewBankTest {
    public static final Currency PIVOT_CURRENCY = EUR;
    private final NewBank bank = NewBank.withPivotCurrency(PIVOT_CURRENCY);

    @Test
    @DisplayName("10 EUR -> USD = 12 USD")
    void convertInDollarsWithEURAsPivotCurrency() {
        assertThat(bank.add(createExchangeRate(1.2, USD))
                .flatMap(newBank -> newBank.convert(euros(10), USD)))
                .containsOnRight(dollars(12));
    }
}
```

:green_circle: To make it pass, we need to now handle the exchangeRates inside our implementation
```java
public class NewBank {
    private final Currency pivotCurrency;
    private final Map<String, ExchangeRate> exchangeRates;

    private NewBank(Currency pivotCurrency, Map<String, ExchangeRate> exchangeRates) {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    private NewBank(Currency pivotCurrency) {
        this(pivotCurrency, empty());
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return !isSameCurrency(exchangeRate.getCurrency(), pivotCurrency)
                ? Right(addRate(exchangeRate))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    private boolean isSameCurrency(Currency exchangeRate, Currency pivotCurrency) {
        return exchangeRate == pivotCurrency;
    }

    private NewBank addRate(ExchangeRate exchangeRate) {
        return new NewBank(
                pivotCurrency,
                exchangeRates.put(keyFor(pivotCurrency, exchangeRate.getCurrency()), exchangeRate)
        );
    }

    private static String keyFor(Currency from, Currency to) {
        return from + "->" + to;
    }

    public Either<Error, Money> convert(Money money, Currency to) {
        return canConvert(money, to)
                ? Right(convertSafely(money, to))
                : Left(new Error("No exchange rate defined for " + money.currency() + "->" + to));
    }

    private boolean canConvert(Money money, Currency to) {
        return isSameCurrency(money.currency(), to) ||
                exchangeRates.containsKey(keyFor(money.currency(), to));
    }

    private Money convertSafely(Money money, Currency to) {
        if (isSameCurrency(money.currency(), to)) {
            return money;
        } else {
            var exchangeRate = exchangeRates.getOrElse(keyFor(money.currency(), to), new ExchangeRate(0, to));
            return new Money(money.amount() * exchangeRate.getRate(), to);
        }
    }
}
```

:large_blue_circle: Make some refactoring to improve readability
```java
public class NewBank {
    private final Currency pivotCurrency;
    private final Map<String, ExchangeRate> exchangeRates;

    private NewBank(Currency pivotCurrency, Map<String, ExchangeRate> exchangeRates) {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    private NewBank(Currency pivotCurrency) {
        this(pivotCurrency, empty());
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return !isSameCurrency(exchangeRate.getCurrency(), pivotCurrency)
                ? Right(addRate(exchangeRate))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    private boolean isSameCurrency(Currency exchangeRate, Currency pivotCurrency) {
        return exchangeRate == pivotCurrency;
    }

    private NewBank addRate(ExchangeRate exchangeRate) {
        return new NewBank(
                pivotCurrency,
                exchangeRates.put(keyFor(pivotCurrency, exchangeRate.getCurrency()), exchangeRate)
        );
    }

    private static String keyFor(Currency from, Currency to) {
        return from + "->" + to;
    }

    public Either<Error, Money> convert(Money money, Currency to) {
        return canConvert(money, to)
                ? Right(convertSafely(money, to))
                : Left(new Error("No exchange rate defined for " + keyFor(money.currency(), to)));
    }

    private boolean canConvert(Money money, Currency to) {
        return isSameCurrency(money.currency(), to) ||
                canConvertDirectly(money, to);
    }

    private boolean canConvertDirectly(Money money, Currency to) {
        return exchangeRates.containsKey(keyFor(money.currency(), to));
    }

    private Money convertSafely(Money money, Currency to) {
        return isSameCurrency(money.currency(), to)
                ? money
                : convertFromPivotCurrency(money, to);
    }

    private Money convertFromPivotCurrency(Money money, Currency to) {
        var exchangeRate = exchangeRates.getOrElse(keyFor(money.currency(), to), new ExchangeRate(0, to));
        return new Money(money.amount() * exchangeRate.getRate(), to);
    }
}
```

Let's add the last example: convert through pivot currency.
```gherkin
Given a Bank with Euro as Pivot Currency and an exchange rate of 1.2 defined for Dollar and an exchange rate of 1344 defined for Korean Wons
When I convert 10 Dollars to Korean Wons
Then it should return 11 200 Korean Wons
```

:red_circle: Add our new example in our tests:
```java
@Test
@DisplayName("10 USD -> KRW = 11 200 KRW")
void convertThroughPivotCurrency() {
    assertThat(bank.add(rateFor(1.2, USD))
            .flatMap(b -> b.add(rateFor(1344, KRW)))
            .flatMap(newBank -> newBank.convert(dollars(10), KRW)))
            .containsOnRight(koreanWons(11200));
}
```

:green_circle: We need to handle how to convert through pivot currency between 2 currencies.
In terms of design one easy way to handle it is to register multiplier and divider exchange rates when adding an exchange rate.
We need to check as well at the conversion time if we can make the conversion:
- To the same currency
- From pivot to a known currency (direct conversion)
- From a known currency to another know one (conversion through pivot currency)

```java
public class NewBank {
    private final Currency pivotCurrency;
    private final Map<String, ExchangeRate> exchangeRates;

    private NewBank(Currency pivotCurrency, Map<String, ExchangeRate> exchangeRates) {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    private NewBank(Currency pivotCurrency) {
        this(pivotCurrency, empty());
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return !isSameCurrency(exchangeRate.getCurrency(), pivotCurrency)
                ? Right(addMultiplierAndDividerExchangeRate(exchangeRate))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    private boolean isSameCurrency(Currency exchangeRate, Currency pivotCurrency) {
        return exchangeRate == pivotCurrency;
    }

    private NewBank addMultiplierAndDividerExchangeRate(ExchangeRate exchangeRate) {
        return new NewBank(
                pivotCurrency,
                exchangeRates.put(keyFor(pivotCurrency, exchangeRate.getCurrency()), exchangeRate)
                        .put(keyFor(exchangeRate.getCurrency(), pivotCurrency), dividerRate(exchangeRate))
        );
    }

    private ExchangeRate dividerRate(ExchangeRate exchangeRate) {
        return new ExchangeRate(1 / exchangeRate.getRate(), exchangeRate.getCurrency());
    }

    private static String keyFor(Currency from, Currency to) {
        return from + "->" + to;
    }

    public Either<Error, Money> convert(Money money, Currency to) {
        return canConvert(money, to)
                ? Right(convertSafely(money, to))
                : Left(new Error("No exchange rate defined for " + keyFor(money.currency(), to)));
    }

    private boolean canConvert(Money money, Currency to) {
        return isSameCurrency(money.currency(), to) ||
                canConvertDirectly(money, to) ||
                canConvertThroughPivotCurrency(money, to);
    }

    private boolean canConvertDirectly(Money money, Currency to) {
        return exchangeRates.containsKey(keyFor(money.currency(), to));
    }

    private boolean canConvertThroughPivotCurrency(Money money, Currency to) {
        return exchangeRates.containsKey(keyFor(pivotCurrency, money.currency()))
                && exchangeRates.containsKey(keyFor(pivotCurrency, to));
    }

    private Money convertSafely(Money money, Currency to) {
        if (isSameCurrency(money.currency(), to))
            return money;

        return canConvertDirectly(money, to)
                ? convertDirectly(money, to)
                : convertThroughPivotCurrency(money, to);
    }

    private Money convertThroughPivotCurrency(Money money, Currency to) {
        var convertToPivot = convertDirectly(money, pivotCurrency);
        var convertToToCurrency = convertDirectly(convertToPivot, to);

        return convertToToCurrency;
    }

    private Money convertDirectly(Money money, Currency to) {
        var exchangeRate = exchangeRates.getOrElse(keyFor(money.currency(), to), new ExchangeRate(0, to));
        return new Money(money.amount() * exchangeRate.getRate(), to);
    }
}
```

:large_blue_circle: Simplify the code and its readability.
```java
public class NewBank {
    private final Currency pivotCurrency;
    private final Map<String, ExchangeRate> exchangeRates;

    private NewBank(Currency pivotCurrency, Map<String, ExchangeRate> exchangeRates) {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    private NewBank(Currency pivotCurrency) {
        this(pivotCurrency, empty());
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return !isSameCurrency(exchangeRate.getCurrency(), pivotCurrency)
                ? Right(addMultiplierAndDividerExchangeRate(exchangeRate))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    private boolean isSameCurrency(Currency exchangeRate, Currency pivotCurrency) {
        return exchangeRate == pivotCurrency;
    }

    private NewBank addMultiplierAndDividerExchangeRate(ExchangeRate exchangeRate) {
        return new NewBank(
                pivotCurrency,
                exchangeRates.put(keyFor(pivotCurrency, exchangeRate.getCurrency()), exchangeRate)
                        .put(keyFor(exchangeRate.getCurrency(), pivotCurrency), dividerRate(exchangeRate))
        );
    }

    private ExchangeRate dividerRate(ExchangeRate exchangeRate) {
        return new ExchangeRate(1 / exchangeRate.getRate(), exchangeRate.getCurrency());
    }

    private static String keyFor(Currency from, Currency to) {
        return from + "->" + to;
    }

    public Either<Error, Money> convert(Money money, Currency to) {
        return canConvert(money, to)
                ? Right(convertSafely(money, to))
                : Left(new Error("No exchange rate defined for " + keyFor(money.currency(), to)));
    }

    private boolean canConvert(Money money, Currency to) {
        return isSameCurrency(money.currency(), to) ||
                canConvertDirectly(money, to) ||
                canConvertThroughPivotCurrency(money, to);
    }

    private boolean canConvertDirectly(Money money, Currency to) {
        return exchangeRates.containsKey(keyFor(money.currency(), to));
    }

    private boolean canConvertThroughPivotCurrency(Money money, Currency to) {
        return exchangeRates.containsKey(keyFor(pivotCurrency, money.currency()))
                && exchangeRates.containsKey(keyFor(pivotCurrency, to));
    }

    private Money convertSafely(Money money, Currency to) {
        if (isSameCurrency(money.currency(), to))
            return money;

        return canConvertDirectly(money, to)
                ? convertDirectly(money, to)
                : convertThroughPivotCurrency(money, to);
    }

    private Money convertDirectly(Money money, Currency to) {
        var exchangeRate = exchangeRates.getOrElse(keyFor(money.currency(), to), new ExchangeRate(0, to));
        return new Money(money.amount() * exchangeRate.getRate(), to);
    }

    private Money convertThroughPivotCurrency(Money money, Currency to) {
        return convertDirectly(convertDirectly(money, pivotCurrency), to);
    }
}
```

- Could we simplify `canConvert` and `convert` methods? We may use a function map to simplify this code and reduce its cyclomatic complexity.

```java
public class NewBank {
    private final Currency pivotCurrency;
    private final Map<String, ExchangeRate> exchangeRates;

    private final Map<Function2<Money, Currency, Boolean>, Function2<Money, Currency, Money>> convert = of(
            (money, to) -> isSameCurrency(money.currency(), to), (money, to) -> money,
            this::canConvertDirectly, this::convertDirectly,
            this::canConvertThroughPivotCurrency, this::convertThroughPivotCurrency
    );

    private NewBank(Currency pivotCurrency, Map<String, ExchangeRate> exchangeRates) {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    private NewBank(Currency pivotCurrency) {
        this(pivotCurrency, empty());
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return !isSameCurrency(exchangeRate.getCurrency(), pivotCurrency)
                ? Right(addMultiplierAndDividerExchangeRate(exchangeRate))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    private boolean isSameCurrency(Currency currency, Currency otherCurrency) {
        return currency == otherCurrency;
    }

    private NewBank addMultiplierAndDividerExchangeRate(ExchangeRate exchangeRate) {
        return new NewBank(
                pivotCurrency,
                exchangeRates.put(keyFor(pivotCurrency, exchangeRate.getCurrency()), exchangeRate)
                        .put(keyFor(exchangeRate.getCurrency(), pivotCurrency), dividerRate(exchangeRate))
        );
    }

    private ExchangeRate dividerRate(ExchangeRate exchangeRate) {
        return new ExchangeRate(1 / exchangeRate.getRate(), exchangeRate.getCurrency());
    }

    private static String keyFor(Currency from, Currency to) {
        return from + "->" + to;
    }

    public Either<Error, Money> convert(Money money, Currency to) {
        return convert.find(canConvert -> canConvert._1.apply(money, to))
                .map(k -> k._2.apply(money, to))
                .toEither(new Error("No exchange rate defined for " + keyFor(money.currency(), to)));
    }

    private boolean canConvertDirectly(Money money, Currency to) {
        return exchangeRates.containsKey(keyFor(money.currency(), to));
    }

    private boolean canConvertThroughPivotCurrency(Money money, Currency to) {
        return exchangeRates.containsKey(keyFor(pivotCurrency, money.currency()))
                && exchangeRates.containsKey(keyFor(pivotCurrency, to));
    }

    private Money convertDirectly(Money money, Currency to) {
        var exchangeRate = exchangeRates.getOrElse(keyFor(money.currency(), to), new ExchangeRate(0, to));
        return new Money(money.amount() * exchangeRate.getRate(), to);
    }

    private Money convertThroughPivotCurrency(Money money, Currency to) {
        return convertDirectly(convertDirectly(money, pivotCurrency), to);
    }
} 
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
:large_blue_circle: Centralize all the conversion tests in the same parameterized test:
```java
class NewBankTest {
    public static final Currency PIVOT_CURRENCY = EUR;
    private final NewBank bank = NewBank.withPivotCurrency(PIVOT_CURRENCY);

    private static Stream<Arguments> examplesForConvertingThroughPivotCurrency() {
        return Stream.of(
                of(dollars(10), KRW, koreanWons(11200)),
                of(dollars(-1), KRW, koreanWons(-1120)),
                of(koreanWons(39_345.50), USD, dollars(35.129910714285714)),
                of(koreanWons(1000), USD, dollars(0.8928571428571427)),
                of(euros(10), USD, dollars(12)),
                of(euros(543.98), USD, dollars(652.776)),
                of(dollars(87), EUR, euros(72.5)),
                of(koreanWons(1_009_765), EUR, euros(751.313244047619))
        );
    }

    @ParameterizedTest
    @MethodSource("examplesForConvertingThroughPivotCurrency")
    void convertShouldReturnExpectedMoney(Money money, Currency to, Money expectedResult) {
        assertThat(bank.add(rateFor(1.2, USD))
                .flatMap(b -> b.add(rateFor(1344, KRW)))
                .flatMap(newBank -> newBank.convert(money, to)))
                .containsOnRight(expectedResult);
    }
}
```

### Strangler on the `Portfolio`
Now that we have defined our new bank implementation using T.D.D outside from the current production code we can intercept and refactor the `Portfolio`.
Let's use another `Strangler`

:red_circle: We start by a failing test / a new expectation


TODO : 
- strangler on Portfolio -> evaluate
- remove old Bank implementation
- use Error instead of Strings [Bonus]
