package money_problem.unit.usecases;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import money_problem.domain.Currency;
import money_problem.domain.Portfolio;
import money_problem.usecases.add_money_in_portfolio.AddInPortfolio;
import money_problem.usecases.add_money_in_portfolio.AddMoneyInPortfolioUseCase;
import money_problem.usecases.ports.PortfolioRepository;
import org.junit.runner.RunWith;

import static money_problem.unit.domain.properties.MoneyGenerator.MAX_AMOUNT;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(JUnitQuickcheck.class)
public class AddMoneyInPortfolioTest {
    private final PortfolioRepository portfolioRepositoryMock = mock(PortfolioRepository.class);
    private final AddMoneyInPortfolioUseCase addMoneyInPortfolioUseCase = new AddMoneyInPortfolioUseCase(portfolioRepositoryMock);

    @Property
    public void return_a_success_for_any_amount_and_rate(@From(AddInPortfolioGenerator.class) AddInPortfolio command) {
        existingPortfolio();
        assertThat(addMoneyInPortfolioUseCase.invoke(command)).isRight();
        portfolioHasBeenSaved();
    }

    private void existingPortfolio() {
        when(portfolioRepositoryMock.get()).thenReturn(new Portfolio());
    }

    private void portfolioHasBeenSaved() {
        verify(portfolioRepositoryMock, times(1)).save(any(Portfolio.class));
    }

    public static class AddInPortfolioGenerator extends Generator<AddInPortfolio> {
        public AddInPortfolioGenerator() {
            super(AddInPortfolio.class);
        }

        @Override
        public AddInPortfolio generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
            return new AddInPortfolio(
                    sourceOfRandomness.nextDouble(-MAX_AMOUNT, MAX_AMOUNT),
                    sourceOfRandomness.choose(Currency.values())
            );
        }
    }
}


