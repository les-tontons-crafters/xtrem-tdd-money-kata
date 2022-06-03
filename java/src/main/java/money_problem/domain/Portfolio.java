package money_problem.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public ConversionResult<String> evaluate(Bank bank, Currency toCurrency) {
        var convertedMoneys = convertAllMoneys(bank, toCurrency);

        return containsFailure(convertedMoneys)
                ? ConversionResult.fromFailure(toFailure(convertedMoneys))
                : ConversionResult.fromSuccess(sumConvertedMoney(convertedMoneys, toCurrency));
    }

    private Money sumConvertedMoney(List<ConversionResult<MissingExchangeRateException>> convertedMoneys, Currency toCurrency) {
        return new Money(convertedMoneys.stream()
                .filter(ConversionResult::isSuccess)
                .mapToDouble(c -> c.money().amount())
                .sum(), toCurrency);
    }

    private String toFailure(List<ConversionResult<MissingExchangeRateException>> convertedMoneys) {
        return convertedMoneys.stream()
                .filter(ConversionResult::isFailure)
                .map(ConversionResult::failure)
                .map(e -> String.format("[%s]", e.getMessage()))
                .collect(Collectors.joining(",", "Missing exchange rate(s): ", ""));
    }

    private boolean containsFailure(List<ConversionResult<MissingExchangeRateException>> convertedMoneys) {
        return convertedMoneys.stream().anyMatch(ConversionResult::isFailure);
    }

    private List<ConversionResult<MissingExchangeRateException>> convertAllMoneys(Bank bank, Currency toCurrency) {
        return moneys.stream()
                .map(money -> convertMoney(bank, money, toCurrency))
                .toList();
    }

    private ConversionResult<MissingExchangeRateException> convertMoney(Bank bank, Money money, Currency toCurrency) {
        try {
            return ConversionResult.fromSuccess(bank.convert(money, toCurrency));
        } catch (MissingExchangeRateException missingExchangeRateException) {
            return ConversionResult.fromFailure(missingExchangeRateException);
        }
    }
}