package money_problem.usecases.evaluate_portfolio;

import io.vavr.control.Either;
import money_problem.usecases.common.UseCaseError;
import money_problem.usecases.ports.BankRepository;
import money_problem.usecases.ports.PortfolioRepository;

public class EvaluatePortfolioUseCase {
    private final BankRepository bankRepository;
    private final PortfolioRepository portfolioRepository;

    public EvaluatePortfolioUseCase(BankRepository bankRepository, PortfolioRepository portfolioRepository) {
        this.bankRepository = bankRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public Either<UseCaseError, EvaluationResult> invoke(EvaluatePortfolio evaluatePortfolio) {
        return bankRepository.getBank()
                .toEither(new UseCaseError("No bank defined"))
                .map(b -> EvaluationResult.ZERO);
    }
}
