package money_problem.usecases.common;

import static money_problem.usecases.common.Unit.unit;

public record Success<T>(T value) {
    public static Success<Unit> emptySuccess() {
        return new Success<>(unit());
    }
}