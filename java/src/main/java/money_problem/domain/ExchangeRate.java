package money_problem.domain;

import io.vavr.control.Either;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

public class ExchangeRate {
    private final double rate;
    private final Currency currency;

    public ExchangeRate(double rate, Currency currency) {
        this.rate = rate;
        this.currency = currency;
    }

    private static boolean isPositive(double rate) {
        return rate > 0;
    }

    public static Either<Error, ExchangeRate> from(double rate, Currency currency) {
        return isPositive(rate)
                ? Right(new ExchangeRate(rate, currency))
                : Left(new Error("Exchange rate should be greater than 0"));
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getRate() {
        return rate;
    }
}