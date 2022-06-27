package money_problem.domain;

import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Either;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

public final class Portfolio {
    private final Seq<Money> moneys;

    public Portfolio() {
        this.moneys = Vector.empty();
    }

    private Portfolio(Seq<Money> moneys) {
        this.moneys = moneys;
    }

    public Portfolio add(Money money) {
        return new Portfolio(moneys.append(money));
    }

    public Either<String, Money> evaluate(Bank bank, Currency toCurrency) {
        var convertedMoneys = convertAllMoneys(bank, toCurrency);

        return containsFailure(convertedMoneys)
                ? left(toFailure(convertedMoneys))
                : right(sumConvertedMoney(convertedMoneys, toCurrency));
    }

    private Seq<Either<String, Money>> convertAllMoneys(Bank bank, Currency toCurrency) {
        return moneys.map(money -> bank.convert(money, toCurrency));
    }

    private boolean containsFailure(Seq<Either<String, Money>> convertedMoneys) {
        return convertedMoneys.exists(Either::isLeft);
    }

    private String toFailure(Seq<Either<String, Money>> convertedMoneys) {
        return convertedMoneys
                .filter(Either::isLeft)
                .map(e -> String.format("[%s]", e.getLeft()))
                .mkString("Missing exchange rate(s): ", ",", "");
    }

    private Money sumConvertedMoney(Seq<Either<String, Money>> convertedMoneys, Currency toCurrency) {
        return new Money(convertedMoneys
                .filter(Either::isRight)
                .map(e -> e.getOrElse(new Money(0, toCurrency)))
                .map(Money::amount)
                .reduce(Double::sum), toCurrency);
    }
}