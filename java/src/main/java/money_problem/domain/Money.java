package money_problem.domain;

public record Money(double amount, Currency currency) {
    public double times(int times) {
        return amount * times;
    }

    public double divide(int divisor) {
        return amount / divisor;
    }
}