package money_problem.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Portfolio {
    private final List<Money> moneys;

    public Portfolio() {
        this.moneys = new ArrayList<>();
    }

    private Portfolio(List<Money> moneys) {
        this.moneys = Collections.unmodifiableList(moneys);
    }

    public Portfolio add(Money money) {
        var updatedMoneys = new ArrayList<>(moneys);
        updatedMoneys.add(money);

        return new Portfolio(updatedMoneys);
    }

    public Money evaluate(Bank bank, Currency toCurrency) throws MissingExchangeRatesException {
        var convertedMoneys = convertAllMoneys(bank, toCurrency);

        if (containsFailure(convertedMoneys)) {
            throw toMissingExchangeRatesException(convertedMoneys);
        }
        return toMoney(convertedMoneys, toCurrency);
    }

    private boolean containsFailure(List<ConversionResult> convertedMoneys) {
        return convertedMoneys.stream().anyMatch(ConversionResult::isFailure);
    }

    private List<ConversionResult> convertAllMoneys(Bank bank, Currency toCurrency) {
        return moneys.stream()
                .map(money -> convertMoney(bank, money, toCurrency))
                .toList();
    }

    private MissingExchangeRatesException toMissingExchangeRatesException(List<ConversionResult> convertedMoneys) {
        return new MissingExchangeRatesException(
                convertedMoneys.stream()
                        .filter(ConversionResult::isFailure)
                        .map(ConversionResult::missingExchangeRateException)
                        .toList()
        );
    }

    private Money toMoney(List<ConversionResult> convertedMoneys, Currency toCurrency) {
        return new Money(convertedMoneys.stream()
                .filter(ConversionResult::isSuccess)
                .mapToDouble(c -> c.money.amount())
                .sum(), toCurrency);
    }

    private ConversionResult convertMoney(Bank bank, Money money, Currency toCurrency) {
        try {
            return new ConversionResult(bank.convert(money, toCurrency));
        } catch (MissingExchangeRateException missingExchangeRateException) {
            return new ConversionResult(missingExchangeRateException);
        }
    }

    private record ConversionResult(Money money, MissingExchangeRateException missingExchangeRateException) {
        public ConversionResult(Money money) {
            this(money, null);
        }

        public ConversionResult(MissingExchangeRateException missingExchangeRateException) {
            this(null, missingExchangeRateException);
        }

        public boolean isFailure() {
            return missingExchangeRateException != null;
        }

        public boolean isSuccess() {
            return money != null;
        }
    }
}