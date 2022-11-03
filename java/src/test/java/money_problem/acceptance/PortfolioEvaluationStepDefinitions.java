package money_problem.acceptance;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import money_problem.domain.Bank;
import money_problem.domain.Currency;
import money_problem.domain.Money;
import money_problem.domain.Portfolio;
import org.assertj.core.api.Assertions;

import static io.vavr.collection.List.*;
import static money_problem.domain.DomainUtility.*;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

public class PortfolioEvaluationStepDefinitions {
    private Bank bank;
    private Portfolio portfolio;

    @Given("our Bank system with {word} as Pivot Currency")
    public void bankWithPivot(String currency) {
        bank = Bank.withPivotCurrency(parseCurrency(currency));
    }

    private Currency parseCurrency(String currency) {
        return of(Currency.values())
                .find(c -> c.toString().equals(currency))
                .get();
    }

    @And("exchange rate of {double} defined for {word}")
    public void addExchangeRate(double rate, String currency) {
        bank = bank.add(rateFor(rate, parseCurrency(currency))).get();
    }

    @Given("an existing customer")
    public void anExistingCustomer() {
        portfolio = new Portfolio();
    }

    @And("they add {double} {word} on their portfolio")
    public void addInPortfolio(double amount, String currency) {
        portfolio = portfolio.add(new Money(amount, parseCurrency(currency)));
    }

    @When("they evaluate their portfolio in {word} the amount should be closed to {double}")
    public void evaluate(String currency, double expectedAmount) {
        var parsedCurrency = parseCurrency(currency);
        var expectedMoney = new Money(expectedAmount, parsedCurrency);

        assertThat(portfolio.evaluate(bank, parsedCurrency))
                .hasRightValueSatisfying(received -> assertClosedTo(received, expectedMoney));
    }

    private static void assertClosedTo(Money money, Money other) {
        Assertions.assertThat(money.currency()).isEqualTo(other.currency());
        Assertions.assertThat(money.amount()).isCloseTo(other.amount(), offset(0.001d));
    }
}