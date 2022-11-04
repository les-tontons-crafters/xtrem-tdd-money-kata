package money_problem.unit.domain;

import io.vavr.collection.Vector;
import money_problem.domain.Currency;
import money_problem.domain.Error;
import money_problem.domain.ExchangeRate;
import money_problem.domain.Money;
import money_problem.domain.Portfolio;

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

    public static money_problem.domain.Error error(String message) {
        return new Error(message);
    }
}