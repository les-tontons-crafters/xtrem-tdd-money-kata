package money_problem.domain;

import io.vavr.collection.Vector;

import static money_problem.domain.Currency.*;
import static money_problem.domain.ExchangeRate.from;

public class DomainUtility {
    public static final String MINIMUM_RATE = "0.000001";
    public static final String MAXIMUM_RATE = "100000";

    public static Money dollars(double amount) {
        return new Money(amount, USD);
    }

    public static Money euros(double amount) {
        return new Money(amount, EUR);
    }

    public static Money koreanWons(double amount) {
        return new Money(amount, KRW);
    }


    public static Portfolio portfolioWith(Money... moneys) {
        return Vector.of(moneys)
                .foldLeft(new Portfolio(), Portfolio::add);
    }

    public static ExchangeRate rateFor(double rate, Currency currency) {
        return from(rate, currency).get();
    }

    public static Error error(String message) {
        return new Error(message);
    }
}