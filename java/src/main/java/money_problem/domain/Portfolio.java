package money_problem.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static money_problem.domain.ConversionResult.fromFailure;
import static money_problem.domain.ConversionResult.fromSuccess;

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
                ? fromFailure(toFailure(convertedMoneys))
                : fromSuccess(sumConvertedMoney(convertedMoneys, toCurrency));
    }

    private Money sumConvertedMoney(List<ConversionResult<String>> convertedMoneys, Currency toCurrency) {
        return new Money(convertedMoneys.stream()
                .filter(ConversionResult::isSuccess)
                .mapToDouble(c -> c.money().amount())
                .sum(), toCurrency);
    }

    private String toFailure(List<ConversionResult<String>> convertedMoneys) {
        return convertedMoneys.stream()
                .filter(ConversionResult::isFailure)
                .map(ConversionResult::failure)
                .map(e -> String.format("[%s]", e))
                .collect(Collectors.joining(",", "Missing exchange rate(s): ", ""));
    }

    private boolean containsFailure(List<ConversionResult<String>> convertedMoneys) {
        return convertedMoneys
                .stream()
                .anyMatch(ConversionResult::isFailure);
    }

    private List<ConversionResult<String>> convertAllMoneys(Bank bank, Currency toCurrency) {
        return moneys.stream()
                .map(money -> bank.convert(money, toCurrency))
                .toList();
    }
}