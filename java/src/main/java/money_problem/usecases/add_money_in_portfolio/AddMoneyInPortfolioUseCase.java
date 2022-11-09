package money_problem.usecases.add_money_in_portfolio;

import io.vavr.control.Either;
import money_problem.domain.Money;
import money_problem.usecases.common.Success;
import money_problem.usecases.common.Unit;
import money_problem.usecases.common.UseCase;
import money_problem.usecases.common.UseCaseError;
import money_problem.usecases.ports.PortfolioRepository;

import static io.vavr.control.Either.right;
import static money_problem.usecases.common.Success.emptySuccess;

public class AddMoneyInPortfolioUseCase implements UseCase<AddInPortfolio, Unit> {
    private final PortfolioRepository portfolioRepository;

    public AddMoneyInPortfolioUseCase(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Override
    public Either<UseCaseError, Success<Unit>> invoke(AddInPortfolio command) {
        portfolioRepository.save(
                portfolioRepository.get()
                        .add(mapToMoney(command))
        );
        return right(emptySuccess());
    }

    private Money mapToMoney(AddInPortfolio command) {
        return new Money(command.amount(), command.currency());
    }
}
