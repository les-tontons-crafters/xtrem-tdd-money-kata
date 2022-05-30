package money_problem.domain;

public class MoneyCalculator {
    public static double times(double amount, Currency currency, int times) {
        return amount * times;
    }

    public static double divide(double amount, Currency currency, int divisor) {
        return amount / divisor;
    }
}