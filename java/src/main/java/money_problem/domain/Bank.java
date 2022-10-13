package money_problem.domain;

import io.vavr.Function2;
import io.vavr.collection.Map;
import io.vavr.control.Either;

import static io.vavr.collection.HashMap.empty;
import static io.vavr.collection.LinkedHashMap.of;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

public class Bank {
    private final Currency pivotCurrency;
    private final Map<String, ExchangeRate> exchangeRates;

    private final Map<CanConvert, Convert> convert = of(
            (money, to) -> isSameCurrency(money.currency(), to), (money, to) -> money,
            this::canConvertDirectly, this::convertDirectly,
            this::canConvertThroughPivotCurrency, this::convertThroughPivotCurrency
    );

    private Bank(Currency pivotCurrency, Map<String, ExchangeRate> exchangeRates) {
        this.pivotCurrency = pivotCurrency;
        this.exchangeRates = exchangeRates;
    }

    private Bank(Currency pivotCurrency) {
        this(pivotCurrency, empty());
    }

    public static Bank withPivotCurrency(Currency pivotCurrency) {
        return new Bank(pivotCurrency);
    }

    public Either<Error, Bank> add(ExchangeRate exchangeRate) {
        return !isSameCurrency(exchangeRate.getCurrency(), pivotCurrency)
                ? right(addMultiplierAndDividerExchangeRate(exchangeRate))
                : left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    private boolean isSameCurrency(Currency currency, Currency otherCurrency) {
        return currency == otherCurrency;
    }

    private Bank addMultiplierAndDividerExchangeRate(ExchangeRate exchangeRate) {
        return new Bank(
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
                .toEither(new Error(keyFor(money.currency(), to)));
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

    private interface CanConvert extends Function2<Money, Currency, Boolean>  { }
    private interface Convert extends Function2<Money, Currency, Money>  { }
}