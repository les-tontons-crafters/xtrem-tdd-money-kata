package money_problem.unit.usecases;

import money_problem.domain.Currency;
import money_problem.domain.ExchangeRate;
import money_problem.domain.Portfolio;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolio;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolioUseCase;
import money_problem.usecases.ports.BankRepository;
import money_problem.usecases.ports.PortfolioRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.vavr.API.Some;
import static io.vavr.collection.List.of;
import static io.vavr.control.Option.none;
import static money_problem.domain.Bank.withPivotCurrency;
import static money_problem.domain.Currency.*;
import static money_problem.unit.domain.DomainUtility.*;
import static money_problem.usecases.common.UseCaseError.error;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluatePortfolioTest {
    private final BankRepository bankRepositoryMock = mock(BankRepository.class);
    private final PortfolioRepository portfolioRepositoryMock = mock(PortfolioRepository.class);
    private final EvaluatePortfolioUseCase evaluatePortfolioUseCase = new EvaluatePortfolioUseCase(bankRepositoryMock, portfolioRepositoryMock);

    private void setupBank(Currency pivotCurrency, ExchangeRate... rates) {
        when(bankRepositoryMock.getBank())
                .thenReturn(
                        Some(of(rates).foldLeft(
                                withPivotCurrency(pivotCurrency),
                                (b, rate) -> b.add(rate).get())
                        )
                );
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
            setupBank(EUR);
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
            setupBank(EUR);
        }

        @Test
        void when_evaluating_empty_portfolio() {
            setupBank(EUR, rateFor(1.2, USD), rateFor(1344, KRW));
            setupPortfolio(portfolioWith(
                    euros(3992),
                    dollars(4567),
                    koreanWons(-30543),
                    dollars(8967.89))
            );
            assertSuccess(18298.02, USD);
        }

        private void assertSuccess(double expectedAmount, Currency expectedCurrency) {
            assertThat(evaluatePortfolioUseCase.invoke(evaluateIn(USD)))
                    .hasRightValueSatisfying(result -> {
                        Assertions.assertThat(result.amount()).isCloseTo(expectedAmount, offset(0.001d));
                        Assertions.assertThat(result.currency()).isEqualTo(expectedCurrency);
                    });
        }
    }
}
