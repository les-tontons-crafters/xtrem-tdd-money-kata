package money_problem.unit.usecases;

import money_problem.domain.Bank;
import money_problem.domain.Currency;
import money_problem.domain.Portfolio;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolio;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolioUseCase;
import money_problem.usecases.ports.BankRepository;
import money_problem.usecases.ports.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.vavr.API.Some;
import static io.vavr.control.Option.none;
import static money_problem.domain.Currency.EUR;
import static money_problem.unit.domain.DomainUtility.*;
import static money_problem.usecases.common.UseCaseError.error;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluatePortfolioTest {
    private final BankRepository bankRepositoryMock = mock(BankRepository.class);
    private final PortfolioRepository portfolioRepositoryMock = mock(PortfolioRepository.class);
    private final EvaluatePortfolioUseCase evaluatePortfolioUseCase = new EvaluatePortfolioUseCase(bankRepositoryMock, portfolioRepositoryMock);

    private void setupBankWithPivot(Currency pivotCurrency) {
        when(bankRepositoryMock.getBank())
                .thenReturn(Some(Bank.withPivotCurrency(pivotCurrency)));
    }

    private void setupPortfolio(Portfolio portfolio) {
        when(portfolioRepositoryMock.get()).thenReturn(portfolio);
    }

    private EvaluatePortfolio evaluateIn(Currency currency) {
        return new EvaluatePortfolio(currency);
    }

    @Nested
    class return_an_error {
        private void bankNotSetup() {
            when(bankRepositoryMock.getBank()).thenReturn(none());
        }

        @Test
        void when_bank_not_setup() {
            bankNotSetup();
            assertError(evaluateIn(EUR), "No bank defined");
        }

        @Test
        void when_missing_rates() {
            setupBankWithPivot(EUR);
            setupPortfolio(portfolioWith(dollars(1), koreanWons(1)));

            assertError(evaluateIn(EUR), "Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
        }

        private void assertError(EvaluatePortfolio evaluatePortfolio, String message) {
            assertThat(evaluatePortfolioUseCase.invoke(evaluatePortfolio))
                    .containsOnLeft(error(message));
        }
    }

    @Nested
    class return_a_success {
        @BeforeEach
        void setup() {
            setupBankWithPivot(EUR);
        }

        @Test
        void when_evaluating_empty_portfolio() {
        }
    }
}
