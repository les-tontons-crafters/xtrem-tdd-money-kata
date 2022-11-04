package money_problem.usecases.add_exchange_rate;

import io.vavr.control.Either;
import money_problem.domain.Error;
import money_problem.domain.ExchangeRate;
import money_problem.usecases.Success;
import money_problem.usecases.UseCaseError;

import static io.vavr.control.Either.left;
import static money_problem.domain.ExchangeRate.from;
import static money_problem.usecases.UseCaseError.error;

public class AddExchangeRateUseCase {
    public Either<UseCaseError, Success<Void>> invoke(AddExchangeRate addExchangeRate) {
        return from(addExchangeRate.rate(), addExchangeRate.currency())
                .flatMap(this::addExchangeRate)
                .mapLeft(domainError -> error(domainError.message()));
    }

    private Either<Error, Success<Void>> addExchangeRate(ExchangeRate rate) {
        return left(new Error("No bank defined"));
    }
}
