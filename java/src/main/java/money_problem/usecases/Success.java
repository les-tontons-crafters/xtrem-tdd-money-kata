package money_problem.usecases;

public record Success<T>() {
    public static Success<Void> emptySuccess() {
        return new Success<>();
    }
}