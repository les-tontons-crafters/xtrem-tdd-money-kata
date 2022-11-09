package money_problem.acceptance;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import money_problem.domain.Currency;
import money_problem.domain.Money;
import money_problem.usecases.add_exchange_rate.AddExchangeRateCommand;
import money_problem.usecases.add_exchange_rate.AddExchangeRateUseCase;
import money_problem.usecases.add_money_in_portfolio.AddInPortfolio;
import money_problem.usecases.add_money_in_portfolio.AddMoneyInPortfolioUseCase;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolio;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolioUseCase;
import money_problem.usecases.evaluate_portfolio.EvaluationResult;
import money_problem.usecases.setup_bank.SetupBankCommand;
import money_problem.usecases.setup_bank.SetupBankUseCase;
import org.assertj.core.api.Assertions;

import static io.vavr.collection.List.of;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

public class PortfolioEvaluationStepDefinitions {
    private final SetupBankUseCase setupBankUseCase = new SetupBankUseCase(null);
    private final AddExchangeRateUseCase addExchangeRateUseCase = new AddExchangeRateUseCase(null);
    private final AddMoneyInPortfolioUseCase addInPortfolioUseCase = new AddMoneyInPortfolioUseCase();
    private final EvaluatePortfolioUseCase evaluatePortfolioUseCase = new EvaluatePortfolioUseCase();

    @Given("our Bank system with {word} as Pivot Currency")
    public void bankWithPivot(String currency) {
        setupBankUseCase.invoke(new SetupBankCommand(parseCurrency(currency)));
    }

    @And("exchange rate of {double} defined for {word}")
    public void addExchangeRate(double rate, String currency) {
        addExchangeRateUseCase.invoke(new AddExchangeRateCommand(rate, parseCurrency(currency)));
    }

    @Given("an existing portfolio")
    public void anExistingPortfolio() {
    }

    @And("our customer adds {double} {word} on their portfolio")
    public void addInPortfolio(double amount, String currency) {
        addInPortfolioUseCase.invoke(new AddInPortfolio(amount, parseCurrency(currency)));
    }

    @When("they evaluate their portfolio in {word} the amount should be {double}")
    public void evaluate(String currency, double expectedAmount) {
        var parsedCurrency = parseCurrency(currency);
        var evaluationResult = evaluatePortfolioUseCase.invoke(new EvaluatePortfolio(parsedCurrency));

        assertThat(evaluationResult)
                .hasRightValueSatisfying(received -> assertClosedTo(received, new Money(expectedAmount, parsedCurrency)));
    }

    private void assertClosedTo(EvaluationResult evaluationResult, Money expected) {
        Assertions.assertThat(evaluationResult.amount()).isCloseTo(expected.amount(), offset(0.001d));
        Assertions.assertThat(evaluationResult.currency()).isEqualTo(expected.currency());
    }

    private Currency parseCurrency(String currency) {
        return of(Currency.values())
                .find(c -> c.toString().equals(currency))
                .get();
    }
}




