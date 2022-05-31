package money_problem.domain;

import java.util.HashMap;
import java.util.Map;

public final class Bank {
    private final Map<String, Double> exchangeRates;

    private Bank(Map<String, Double> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public static Bank withExchangeRate(Currency from, Currency to, double rate) {
        var bank = new Bank(new HashMap<>());
        bank.addExchangeRate(from, to, rate);

        return bank;
    }

    public void addExchangeRate(Currency from, Currency to, double rate) {
        exchangeRates.put(keyFor(from, to), rate);
    }

    private static String keyFor(Currency from, Currency to) {
        return from + "->" + to;
    }

    public Money convert(Money money, Currency to) throws MissingExchangeRateException {
        if (!canConvert(money, to)) {
            throw new MissingExchangeRateException(money.currency(), to);
        }
        return convertSafely(money, to);
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