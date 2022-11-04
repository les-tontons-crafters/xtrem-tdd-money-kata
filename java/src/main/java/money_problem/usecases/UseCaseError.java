package money_problem.usecases;

public record UseCaseError(String message) {
    public static UseCaseError error(String message) {
        return new UseCaseError(message);
    }
}
