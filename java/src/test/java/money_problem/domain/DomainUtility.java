package money_problem.domain;

import io.vavr.collection.Vector;

public class DomainUtility {
    public static Money dollars(double amount) {
        return new Money(amount, Currency.USD);
    }

    public static Money euros(double amount) {
        return new Money(amount, Currency.EUR);
    }

    public static Money koreanWons(double amount) {
        return new Money(amount, Currency.KRW);
    }

    public static Portfolio portfolioWith(Money... moneys) {
        return Vector.of(moneys)
                .foldLeft(new Portfolio(), Portfolio::add);
    }
}