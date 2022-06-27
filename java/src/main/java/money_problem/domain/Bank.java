package money_problem.domain;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Either;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

public final class Bank {
    private final Map<String, Double> exchangeRates;

    private Bank(Map<String, Double> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public static Bank withExchangeRate(Currency from, Currency to, double rate) {
        var bank = new Bank(HashMap.empty());
        return bank.addExchangeRate(from, to, rate);
    }

    public Bank addExchangeRate(Currency from, Currency to, double rate) {
        return new Bank(exchangeRates.put(keyFor(from, to), rate));
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
        return money.currency() == to
                || exchangeRates.containsKey(keyFor(money.currency(), to));
    }

    private Money convertSafely(Money money, Currency to) {
        return money.currency() == to
                ? money
                : new Money(money.amount() * exchangeRates.getOrElse(keyFor(money.currency(), to), 0d), to);
    }
}