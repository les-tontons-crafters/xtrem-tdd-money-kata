package money_problem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static money_problem.domain.Currency.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioTest {
    private Bank bank;

    @BeforeEach
    void setup() {
        bank = Bank.withExchangeRate(EUR, USD, 1.2);
        bank.addExchangeRate(USD, KRW, 1100);
    }

    @Test
    @DisplayName("5 USD + 10 USD = 15 USD")
    void shouldAddMoneyInTheSameCurrency() throws MissingExchangeRatesException {
        var portfolio = new Portfolio();
        portfolio.add(new Money(5, USD));
        portfolio.add(new Money(10, USD));

        assertThat(portfolio.evaluate(bank, USD))
                .isEqualTo(new Money(15, USD));
    }

    @Test
    @DisplayName("5 USD + 10 EUR = 17 USD")
    void shouldAddMoneyInDollarsAndEuros() throws MissingExchangeRatesException {
        var portfolio = new Portfolio();
        portfolio.add(new Money(5, USD));
        portfolio.add(new Money(10, EUR));

        assertThat(portfolio.evaluate(bank, USD))
                .isEqualTo(new Money(17, USD));
    }

    @Test
    @DisplayName("1 USD + 1100 KRW = 2200 KRW")
    void shouldAddMoneyInDollarsAndKoreanWons() throws MissingExchangeRatesException {
        var portfolio = new Portfolio();
        portfolio.add(new Money(1, USD));
        portfolio.add(new Money(1100, KRW));

        assertThat(portfolio.evaluate(bank, KRW))
                .isEqualTo(new Money(2200, KRW));
    }

    @Test
    @DisplayName("5 USD + 10 EUR + 4 EUR = 21.8 USD")
    void shouldAddMoneyInDollarsAndMultipleAmountInEuros() throws MissingExchangeRatesException {
        var portfolio = new Portfolio();
        portfolio.add(new Money(5, USD));
        portfolio.add(new Money(10, EUR));
        portfolio.add(new Money(4, EUR));

        assertThat(portfolio.evaluate(bank, USD))
                .isEqualTo(new Money(21.8, USD));
    }

    @Test
    @DisplayName("Throws a MissingExchangeRatesException in case of missing exchange rates")
    void shouldThrowAMissingExchangeRatesException() {
        var portfolio = new Portfolio();
        portfolio.add(new Money(1, EUR));
        portfolio.add(new Money(1, USD));
        portfolio.add(new Money(1, KRW));

        assertThatThrownBy(() -> portfolio.evaluate(bank, EUR))
                .isInstanceOf(MissingExchangeRatesException.class)
                .hasMessage("Missing exchange rate(s): [USD->EUR],[KRW->EUR]");
    }
}

