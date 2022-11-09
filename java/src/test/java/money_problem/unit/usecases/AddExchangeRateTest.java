package money_problem.unit.usecases;

import money_problem.domain.Bank;
import money_problem.domain.Currency;
import money_problem.usecases.add_exchange_rate.AddExchangeRateCommand;
import money_problem.usecases.add_exchange_rate.AddExchangeRateUseCase;
import money_problem.usecases.ports.BankRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.vavr.API.Some;
import static io.vavr.control.Option.none;
import static money_problem.domain.Currency.EUR;
import static money_problem.domain.Currency.USD;
import static money_problem.usecases.common.Unit.unit;
import static money_problem.usecases.common.UseCaseError.error;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddExchangeRateTest {
    private final BankRepository bankRepositoryMock = mock(BankRepository.class);
    private final AddExchangeRateUseCase addExchangeRateUseCase = new AddExchangeRateUseCase(bankRepositoryMock);

    private void setupBankWithPivot(Currency pivotCurrency) {
        when(bankRepositoryMock.getBank())
                .thenReturn(Some(Bank.withPivotCurrency(pivotCurrency)));
    }

    private void bankHasBeenSaved() {
        verify(bankRepositoryMock, times(1))
                .save(any(Bank.class));
    }

    @Nested
    class return_an_error {
        @BeforeEach
        void setup() {
            when(bankRepositoryMock.getBank()).thenReturn(none());
        }

        @Test
        void when_bank_not_setup() {
            var aValidExchangeRate = new AddExchangeRateCommand(1, USD);
            assertError(aValidExchangeRate, "No bank defined");
        }

        @Test
        void when_exchange_rate_is_invalid() {
            var invalidExchangeRate = new AddExchangeRateCommand(-2, USD);
            assertError(invalidExchangeRate, "Exchange rate should be greater than 0");
        }

        @Test
        void when_passing_a_rate_for_pivot_currency() {
            var pivotCurrency = EUR;
            var exchangeRateForPivot = new AddExchangeRateCommand(0.9, pivotCurrency);

            setupBankWithPivot(pivotCurrency);

            assertError(exchangeRateForPivot, "Can not add an exchange rate for the pivot currency");
        }

        private void assertError(AddExchangeRateCommand invalidExchangeRate, String message) {
            assertThat(addExchangeRateUseCase.invoke(invalidExchangeRate))
                    .containsOnLeft(error(message));
        }
    }

    @Nested
    class return_a_success {
        @Test
        void when_passing_a_valid_rate_for_a_currency_different_than_pivot() {
            setupBankWithPivot(EUR);
            var aValidExchangeRate = new AddExchangeRateCommand(1, USD);

            assertThat(addExchangeRateUseCase.invoke(aValidExchangeRate))
                    .containsOnRight(unit());

            bankHasBeenSaved();
        }
    }
}
