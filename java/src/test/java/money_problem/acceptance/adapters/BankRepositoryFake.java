package money_problem.acceptance.adapters;

import io.vavr.control.Option;
import money_problem.domain.Bank;
import money_problem.usecases.ports.BankRepository;

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

public class BankRepositoryFake implements BankRepository {
    private Bank bank;

    @Override
    public boolean exists() {
        return bank != null;
    }

    @Override
    public Option<Bank> getBank() {
        return exists()
                ? some(bank)
                : none();
    }

    @Override
    public void save(Bank bank) {
        this.bank = bank;
    }
}
