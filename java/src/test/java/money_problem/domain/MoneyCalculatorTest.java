package money_problem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.*;
import static org.assertj.core.api.Assertions.assertThat;

class MoneyCalculatorTest {
    @Test
    @DisplayName("5 USD + 10 USD = 15 USD")
    void shouldAddInUsd() {
        assertThat(MoneyCalculator.add(5, USD, 10))
                .isEqualTo(15);
    }

    @Test
    @DisplayName("10 EUR x 2 = 20 EUR")
    void shouldMultiplyInEuros() {
        assertThat(MoneyCalculator.times(10, EUR, 2))
                .isEqualTo(20);
    }

    @Test
    @DisplayName("4002 KRW / 4 = 1000.5 KRW")
    void shouldDivideInKoreanWons() {
        assertThat(MoneyCalculator.divide(4002, KRW, 4))
                .isEqualTo(1000.5);
    }
}