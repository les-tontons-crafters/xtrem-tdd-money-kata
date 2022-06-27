package money_problem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.*;
import static money_problem.domain.DomainUtility.*;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

class PortfolioTest {
    private Bank bank;

    @BeforeEach
    void setup() {
        bank = Bank.withExchangeRate(EUR, USD, 1.2)
                .addExchangeRate(USD, KRW, 1100);
    }

    @Test
    @DisplayName("5 USD + 10 USD = 15 USD")
    void shouldAddMoneyInTheSameCurrency() {
        var portfolio = portfolioWith(
                dollars(5),
                dollars(10)
        );

        assertThat(portfolio.evaluate(bank, USD))
                .containsOnRight(dollars(15));
    }

    @Test
    @DisplayName("5 USD + 10 EUR = 17 USD")
    void shouldAddMoneyInDollarsAndEuros() {
        var portfolio = portfolioWith(
                dollars(5),
                euros(10)
        );

        assertThat(portfolio.evaluate(bank, USD))
                .containsOnRight(dollars(17));
    }

    @Test
    @DisplayName("1 USD + 1100 KRW = 2200 KRW")
    void shouldAddMoneyInDollarsAndKoreanWons() {
        var portfolio = portfolioWith(
                dollars(1),
                koreanWons(1100)
        );

        assertThat(portfolio.evaluate(bank, KRW))
                .containsOnRight(koreanWons(2200));
    }

    @Test
    @DisplayName("5 USD + 10 EUR + 4 EUR = 21.8 USD")
    void shouldAddMoneyInDollarsAndMultipleAmountInEuros() {
        var portfolio = portfolioWith(
                dollars(5),
                euros(10),
                euros(4)
        );

        assertThat(portfolio.evaluate(bank, USD))
                .containsOnRight(dollars(21.8));
    }

    @Test
    @DisplayName("Return a failure result in case of missing exchange rates")
    void shouldReturnAFailingResultInCaseOfMissingExchangeRates() {
        var portfolio = portfolioWith(
                euros(1),
                dollars(1),
                koreanWons(1)
        );

        assertThat(portfolio.evaluate(bank, EUR))
                .containsOnLeft("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
    }
}