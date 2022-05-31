package money_problem.domain;

import java.util.ArrayList;

public class Portfolio {
    private final ArrayList<Money> moneys = new ArrayList<>();

    public void add(Money money) {
        moneys.add(money);
    }

    public Money evaluate(Bank bank, Currency toCurrency) throws MissingExchangeRatesException {
        var convertedResult = 0d;
        var missingExchangeRates = new ArrayList<MissingExchangeRateException>();

        for (Money money : moneys) {
            try {
                var convertedAmount = bank.convert(money, toCurrency);
                convertedResult += convertedAmount.amount();
            } catch (MissingExchangeRateException missingExchangeRateException) {
                missingExchangeRates.add(missingExchangeRateException);
            }
        }

        if (!missingExchangeRates.isEmpty()) {
            throw new MissingExchangeRatesException(missingExchangeRates);
        }
        return new Money(convertedResult, toCurrency);
    }
}