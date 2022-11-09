package money_problem.acceptance.adapters;

import money_problem.domain.Portfolio;
import money_problem.usecases.ports.PortfolioRepository;

public class PortfolioRepositoryFake implements PortfolioRepository {
    private Portfolio portfolio;

    @Override
    public Portfolio get() {
        return portfolio;
    }

    @Override
    public void save(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
}
