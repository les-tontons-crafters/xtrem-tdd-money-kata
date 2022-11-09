package money_problem.usecases.setup_bank;

import io.vavr.control.Either;
import money_problem.domain.Currency;
import money_problem.usecases.common.Unit;
import money_problem.usecases.common.UseCase;
import money_problem.usecases.common.UseCaseError;
import money_problem.usecases.ports.BankRepository;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static money_problem.domain.Bank.withPivotCurrency;
import static money_problem.usecases.common.Unit.unit;
import static money_problem.usecases.common.UseCaseError.error;

public class SetupBankUseCase implements UseCase<SetupBankCommand, Unit> {
    private final BankRepository bankRepository;

    public SetupBankUseCase(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @Override
    public Either<UseCaseError, Unit> invoke(SetupBankCommand setupBank) {
        return bankRepository.exists()
                ? left(error("Bank is already setup"))
                : right(setupBank(setupBank.currency()));
    }

    private Unit setupBank(Currency currency) {
        bankRepository.save(withPivotCurrency(currency));
        return unit();
    }
}