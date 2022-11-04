package money_problem.usecases;

import io.vavr.control.Either;

public interface UseCase<TCommand extends Command, TSuccess> {
    Either<UseCaseError, Success<TSuccess>> invoke(TCommand command);
}