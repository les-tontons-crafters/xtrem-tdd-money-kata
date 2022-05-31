package money_problem.domain;

import java.util.ArrayList;

public class Portfolio {
    private final ArrayList<Money> moneys = new ArrayList<>();

    public void add(double amount, Currency currency) {
        add(new Money(amount, currency));
    }

    public void add(Money money) {
        moneys.add(money);
    }

    public double evaluate(Bank bank, Currency toCurrency) throws MissingExchangeRatesException {
        var convertedResult = 0d;
        var missingExchangeRates = new ArrayList<MissingExchangeRateException>();

        for (Money money : moneys) {
            try {
                var convertedAmount = bank.convert(money.amount(), money.currency(), toCurrency);
                convertedResult += convertedAmount;
            } catch (MissingExchangeRateException missingExchangeRateException) {
                missingExchangeRates.add(missingExchangeRateException);
            }
        }

        if (!missingExchangeRates.isEmpty()) {
            throw new MissingExchangeRatesException(missingExchangeRates);
        }
        return convertedResult;
    }
}