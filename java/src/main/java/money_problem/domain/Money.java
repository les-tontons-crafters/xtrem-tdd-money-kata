package money_problem.domain;

public final record Money(double amount, Currency currency) {
    public Money times(int times) {
        return new Money(amount * times, currency);
    }

    public Money divide(int divisor) {
        return new Money(amount / divisor, currency);
    }
}