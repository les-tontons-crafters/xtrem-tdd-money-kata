package money_problem.unit.usecases;

import money_problem.domain.Currency;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolio;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolioUseCase;
import money_problem.usecases.ports.BankRepository;
import money_problem.usecases.ports.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.vavr.control.Option.none;
import static money_problem.usecases.common.UseCaseError.error;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluatePortfolioTest {
    private final BankRepository bankRepositoryMock = mock(BankRepository.class);
    private final PortfolioRepository portfolioRepositoryMock = mock(PortfolioRepository.class);
    private final EvaluatePortfolioUseCase evaluatePortfolioUseCase = new EvaluatePortfolioUseCase(bankRepositoryMock, portfolioRepositoryMock);

    @Nested
    class return_an_error {
        @BeforeEach
        void setup() {
            when(bankRepositoryMock.getBank()).thenReturn(none());
        }

        @Test
        void when_bank_not_setup() {
            assertError(new EvaluatePortfolio(Currency.EUR), "No bank defined");
        }

        private void assertError(EvaluatePortfolio evaluatePortfolio, String message) {
            assertThat(evaluatePortfolioUseCase.invoke(evaluatePortfolio))
                    .containsOnLeft(error(message));
        }
    }
}
