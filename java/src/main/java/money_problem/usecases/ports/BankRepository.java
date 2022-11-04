package money_problem.usecases.ports;

import money_problem.domain.Bank;

public interface BankRepository {
    boolean exists();

    void save(Bank bank);
}