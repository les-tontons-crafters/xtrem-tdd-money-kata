package money_problem.usecases.setup_bank;

import io.vavr.control.Either;
import money_problem.domain.Currency;
import money_problem.usecases.Success;
import money_problem.usecases.UseCaseError;
import money_problem.usecases.ports.BankRepository;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static money_problem.domain.Bank.withPivotCurrency;
import static money_problem.usecases.Success.emptySuccess;
import static money_problem.usecases.UseCaseError.error;

public class SetupBankUseCase {
    private final BankRepository bankRepository;

    public SetupBankUseCase(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public Either<UseCaseError, Success<Void>> invoke(SetupBank setupBank) {
        return bankRepository.exists()
                ? left(error("Bank is already setup"))
                : right(setupBank(setupBank.currency()));
    }

    private Success<Void> setupBank(Currency currency) {
        bankRepository.save(withPivotCurrency(currency));
        return emptySuccess();
    }
}