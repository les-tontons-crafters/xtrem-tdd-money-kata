namespace money_problem.Domain;

public static class MoneyCalculator
{
    public static double Add(double amount, Currency currency, double addedAmount) => amount + addedAmount;
    public static double Times(double amount, Currency currency, int times) => amount * times;
    public static double Divide(double amount, Currency currency, int divisor) => amount / divisor;
}