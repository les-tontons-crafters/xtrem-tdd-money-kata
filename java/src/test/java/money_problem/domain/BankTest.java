package money_problem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.*;
import static money_problem.domain.DomainUtility.dollars;
import static money_problem.domain.DomainUtility.euros;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

class BankTest {
    private final Bank bank = Bank.withExchangeRate(EUR, USD, 1.2);

    @Test
    @DisplayName("10 EUR -> USD = 12 USD")
    void shouldConvertEuroToUsd() {
        assertThat(bank.convert(euros(10), USD))
                .containsOnRight(dollars(12));
    }

    @Test
    @DisplayName("10 EUR -> EUR = 10 EUR")
    void shouldConvertInSameCurrency() {
        assertThat(bank.convert(euros(10), EUR))
                .containsOnRight(euros(10));
    }

    @Test
    @DisplayName("Return a failure result in case of missing exchange rate")
    void shouldReturnAFailingResultInCaseOfMissingExchangeRate() {
        assertThat(bank.convert(euros(10), KRW))
                .containsOnLeft("EUR->KRW");
    }

    @Test
    @DisplayName("Conversion with different exchange rates EUR to USD")
    void shouldConvertWithDifferentExchangeRates() {
        assertThat(bank.convert(euros(10), USD))
                .containsOnRight(dollars(12));

        assertThat(bank.addExchangeRate(EUR, USD, 1.3)
                .convert(euros(10), USD))
                .containsOnRight(dollars(13));
    }
}