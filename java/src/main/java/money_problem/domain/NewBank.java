package money_problem.domain;

import io.vavr.collection.Map;
import io.vavr.control.Either;

import static io.vavr.API.Left;
import static io.vavr.API.Right;
import static io.vavr.collection.HashMap.empty;

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