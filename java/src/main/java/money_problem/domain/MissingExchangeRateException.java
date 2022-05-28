package money_problem.domain;

public class MissingExchangeRateException extends Exception {
    public MissingExchangeRateException(Currency from, Currency to) {
        super(String.format("%s->%s", from, to));
    }
}
