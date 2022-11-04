package money_problem.usecases.common;

public record Success<T>() {
    public static Success<Void> emptySuccess() {
        return new Success<>();
    }
}