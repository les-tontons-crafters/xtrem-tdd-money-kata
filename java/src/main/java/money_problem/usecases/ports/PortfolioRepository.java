package money_problem.usecases.ports;

import money_problem.domain.Portfolio;

public interface PortfolioRepository {
    Portfolio get();

    void save(Portfolio portfolio);
}
