package money_problem.usecases.add_exchange_rate;

import io.vavr.control.Either;
import money_problem.domain.Bank;
import money_problem.domain.Error;
import money_problem.domain.ExchangeRate;
import money_problem.usecases.common.Success;
import money_problem.usecases.common.UseCase;
import money_problem.usecases.common.UseCaseError;
import money_problem.usecases.ports.BankRepository;

import static money_problem.domain.ExchangeRate.from;
import static money_problem.usecases.common.Success.emptySuccess;
import static money_problem.usecases.common.UseCaseError.error;

public class AddExchangeRateUseCase implements UseCase<AddExchangeRate, Void> {
    private final BankRepository bankRepository;

    public AddExchangeRateUseCase(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @Override
    public Either<UseCaseError, Success<Void>> invoke(AddExchangeRate addExchangeRate) {
        return from(addExchangeRate.rate(), addExchangeRate.currency())
                .flatMap(this::addExchangeRate)
                .map(bank -> emptySuccess())
                .mapLeft(domainError -> error(domainError.message()));
    }

    private Either<Error, Bank> addExchangeRate(ExchangeRate rate) {
        return bankRepository.getBank()
                .toEither(new Error("No bank defined"))
                .flatMap(bank -> bank.add(rate))
                .peek(bankRepository::save);
    }
}
