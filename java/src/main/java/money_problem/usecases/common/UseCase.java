package money_problem.usecases.common;

import io.vavr.control.Either;

public interface UseCase<TRequest extends Request, TSuccess> {
    Either<UseCaseError, Success<TSuccess>> invoke(TRequest command);
}