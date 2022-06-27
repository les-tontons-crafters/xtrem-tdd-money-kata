package money_problem.domain;

import io.vavr.control.Either;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

public final class Bank {
    private final Map<String, Double> exchangeRates;

    private Bank(Map<String, Double> exchangeRates) {
        this.exchangeRates = Collections.unmodifiableMap(exchangeRates);
    }

    public static Bank withExchangeRate(Currency from, Currency to, double rate) {
        var bank = new Bank(new HashMap<>());
        return bank.addExchangeRate(from, to, rate);
    }

    public Bank addExchangeRate(Currency from, Currency to, double rate) {
        var updateExchangeRates = new HashMap<>(exchangeRates);
        updateExchangeRates.put(keyFor(from, to), rate);

        return new Bank(updateExchangeRates);
    }

    private static String keyFor(Currency from, Currency to) {
        return from + "->" + to;
    }

    public Either<String, Money> convert(Money money, Currency toCurrency) {
        return canConvert(money, toCurrency)
                ? right(convertSafely(money, toCurrency))
                : left(String.format("%s->%s", money.currency(), toCurrency));
    }

    private boolean canConvert(Money money, Currency to) {
        return money.currency() == to || exchangeRates.containsKey(keyFor(money.currency(), to));
    }

    private Money convertSafely(Money money, Currency to) {
        return money.currency() == to
                ? money
                : new Money(money.amount() * exchangeRates.get(keyFor(money.currency(), to)), to);
    }
}