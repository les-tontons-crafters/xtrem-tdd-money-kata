package money_problem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static money_problem.domain.DomainUtility.euros;
import static money_problem.domain.DomainUtility.koreanWons;
import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {
    @Test
    @DisplayName("10 EUR x 2 = 20 EUR")
    void shouldMultiplyInEuros() {
        assertThat(euros(10)
                .times(2))
                .isEqualTo(euros(20));
    }

    @Test
    @DisplayName("10 EUR x 2 = 20 EUR")
    void shouldMultiplyInEuros2() {
        assertThat(euros(10)
                .times(2))
                .isEqualTo(euros(20));
    }

    @Test
    @DisplayName("4002 KRW / 4 = 1000.5 KRW")
    void shouldDivideInKoreanWons() {
        assertThat(koreanWons(4002)
                .divide(4))
                .isEqualTo(koreanWons(1000.5));
    }
}