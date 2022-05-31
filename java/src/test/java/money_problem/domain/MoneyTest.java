package money_problem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.EUR;
import static money_problem.domain.Currency.KRW;
import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {
    @Test
    @DisplayName("10 EUR x 2 = 20 EUR")
    void shouldMultiplyInEuros() {
        assertThat(new Money(10, EUR).times(2))
                .isEqualTo(20);
    }

    @Test
    @DisplayName("4002 KRW / 4 = 1000.5 KRW")
    void shouldDivideInKoreanWons() {
        assertThat(new Money(4002, KRW).divide(4))
                .isEqualTo(1000.5);
    }
}