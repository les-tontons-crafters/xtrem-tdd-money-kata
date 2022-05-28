package money_problem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankTest {
    private final Bank bank = Bank.withExchangeRate(EUR, USD, 1.2);

    @Test
    @DisplayName("10 EUR -> USD = 12 USD")
    void shouldConvertEuroToUsd() throws MissingExchangeRateException {
        assertThat(bank.convert(10, EUR, USD))
                .isEqualTo(12);
    }

    @Test
    @DisplayName("10 EUR -> EUR = 10 EUR")
    void shouldConvertInSameCurrency() throws MissingExchangeRateException {
        assertThat(bank.convert(10, EUR, EUR))
                .isEqualTo(10);
    }

    @Test
    @DisplayName("Throws a MissingExchangeRateException in case of missing exchange rates")
    void shouldReturnALeftOnMissingExchangeRate() {
        assertThatThrownBy(() -> bank.convert(10, EUR, KRW))
                .isInstanceOf(MissingExchangeRateException.class)
                .hasMessage("EUR->KRW");
    }

    @Test
    @DisplayName("Conversion with different exchange rates EUR to USD")
    void shouldConvertWithDifferentExchangeRates() throws MissingExchangeRateException {
        assertThat(bank.convert(10, EUR, USD))
                .isEqualTo(12);

        bank.addExchangeRate(EUR, USD, 1.3);

        assertThat(bank.convert(10, EUR, USD))
                .isEqualTo(13);
    }
}