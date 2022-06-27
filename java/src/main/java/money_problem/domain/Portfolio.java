package money_problem.domain;

import io.vavr.control.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

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

    public Either<String, Money> evaluate(Bank bank, Currency toCurrency) {
        var convertedMoneys = convertAllMoneys(bank, toCurrency);

        return containsFailure(convertedMoneys)
                ? left(toFailure(convertedMoneys))
                : right(sumConvertedMoney(convertedMoneys, toCurrency));
    }

    private Money sumConvertedMoney(List<Either<String, Money>> convertedMoneys, Currency toCurrency) {
        return new Money(convertedMoneys.stream()
                .filter(Either::isRight)
                .map(e -> e.getOrElse(new Money(0, toCurrency)))
                .mapToDouble(Money::amount)
                .sum(), toCurrency);
    }

    private String toFailure(List<Either<String, Money>> convertedMoneys) {
        return convertedMoneys.stream()
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .map(e -> String.format("[%s]", e))
                .collect(Collectors.joining(",", "Missing exchange rate(s): ", ""));
    }

    private boolean containsFailure(List<Either<String, Money>> convertedMoneys) {
        return convertedMoneys
                .stream()
                .anyMatch(Either::isLeft);
    }

    private List<Either<String, Money>> convertAllMoneys(Bank bank, Currency toCurrency) {
        return moneys.stream()
                .map(money -> bank.convert(money, toCurrency))
                .toList();
    }
}

