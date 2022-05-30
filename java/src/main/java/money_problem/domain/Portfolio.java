package money_problem.domain;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private final Map<Currency, List<Double>> moneys = new EnumMap<>(Currency.class);

    public void add(double amount, Currency currency) {
        moneys.compute(currency, (c, amounts) -> {
            if (amounts == null) {
                amounts = new ArrayList<>();
            }
            amounts.add(amount);
            return amounts;
        });
    }

    public double evaluate(Bank bank, Currency toCurrency) throws MissingExchangeRatesException {
        var convertedResult = 0d;
        var missingExchangeRates = new ArrayList<MissingExchangeRateException>();

        for (Map.Entry<Currency, List<Double>> entry : moneys.entrySet()) {
            for (Double amount : entry.getValue()) {
                try {
                    var convertedAmount = bank.convert(amount, entry.getKey(), toCurrency);
                    convertedResult += convertedAmount;
                } catch (MissingExchangeRateException missingExchangeRateException) {
                    missingExchangeRates.add(missingExchangeRateException);
                }
            }
        }

        if (!missingExchangeRates.isEmpty()) {
            throw new MissingExchangeRatesException(missingExchangeRates);
        }
        return convertedResult;
    }
}