package money_problem.unit.usecases;

import money_problem.usecases.add_exchange_rate.AddExchangeRate;
import money_problem.usecases.add_exchange_rate.AddExchangeRateUseCase;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.EUR;
import static money_problem.domain.Currency.USD;
import static money_problem.usecases.UseCaseError.error;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

class AddExchangeRateTest {
    private final AddExchangeRateUseCase addExchangeRate = new AddExchangeRateUseCase();

    @Test
    void return_an_error_when_bank_not_setup() {
        assertThat(addExchangeRate.invoke(new AddExchangeRate(1, EUR)))
                .containsOnLeft(error("No bank defined"));
    }

    @Test
    void return_an_error_when_exchange_rate_is_invalid() {
        assertThat(addExchangeRate.invoke(new AddExchangeRate(-2, USD)))
                .containsOnLeft(error("Exchange rate should be greater than 0"));
    }
}
