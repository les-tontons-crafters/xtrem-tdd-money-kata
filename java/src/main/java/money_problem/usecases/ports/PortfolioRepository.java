package money_problem.usecases.ports;

import money_problem.domain.Portfolio;

public interface PortfolioRepository {
    void save(Portfolio portfolio);
}
