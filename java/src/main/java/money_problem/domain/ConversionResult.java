package money_problem.domain;

public record ConversionResult<Failure>(Money money, Failure failure) {
    private ConversionResult(Money money) {
        this(money, null);
    }

    private ConversionResult(Failure exception) {
        this(null, exception);
    }

    public static <F> ConversionResult<F> fromFailure(F failure) {
        return new ConversionResult<>(failure);
    }

    public static <F> ConversionResult<F> fromSuccess(Money money) {
        return new ConversionResult<>(money);
    }

    public boolean isFailure() {
        return failure != null;
    }

    public boolean isSuccess() {
        return money != null;
    }
}