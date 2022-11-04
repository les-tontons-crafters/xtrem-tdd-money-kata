package money_problem.usecases.ports;

import io.vavr.control.Option;
import money_problem.domain.Bank;

public interface BankRepository {
    boolean exists();

    Option<Bank> getBank();

    void save(Bank bank);
}