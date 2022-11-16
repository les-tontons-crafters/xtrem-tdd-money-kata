package money_problem.acceptance;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.vavr.Tuple;
import money_problem.acceptance.adapters.BankRepositoryFake;
import money_problem.acceptance.adapters.PortfolioRepositoryFake;
import money_problem.domain.Currency;
import money_problem.domain.Money;
import money_problem.domain.Portfolio;
import money_problem.usecases.add_exchange_rate.AddExchangeRateCommand;
import money_problem.usecases.add_exchange_rate.AddExchangeRateUseCase;
import money_problem.usecases.add_money_in_portfolio.AddInPortfolio;
import money_problem.usecases.add_money_in_portfolio.AddMoneyInPortfolioUseCase;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolio;
import money_problem.usecases.evaluate_portfolio.EvaluatePortfolioUseCase;
import money_problem.usecases.evaluate_portfolio.EvaluationResult;
import money_problem.usecases.ports.BankRepository;
import money_problem.usecases.ports.PortfolioRepository;
import money_problem.usecases.setup_bank.SetupBankCommand;
import money_problem.usecases.setup_bank.SetupBankUseCase;
import org.assertj.core.api.Assertions;

import static io.vavr.collection.List.of;
import static java.lang.Double.parseDouble;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.vavr.api.VavrAssertions.assertThat;

public class PortfolioEvaluationStepDefinitions {
    private final BankRepository bankRepositoryFake = new BankRepositoryFake();
    private final PortfolioRepository portfolioRepositoryFake = new PortfolioRepositoryFake();
    private final SetupBankUseCase setupBankUseCase = new SetupBankUseCase(bankRepositoryFake);
    private final AddExchangeRateUseCase addExchangeRateUseCase = new AddExchangeRateUseCase(bankRepositoryFake);
    private final AddMoneyInPortfolioUseCase addInPortfolioUseCase = new AddMoneyInPortfolioUseCase(portfolioRepositoryFake);
    private final EvaluatePortfolioUseCase evaluatePortfolioUseCase = new EvaluatePortfolioUseCase(bankRepositoryFake, portfolioRepositoryFake);

    @Given("our Bank system with {word} as Pivot Currency")
    public void bankWithPivot(String currency) {
        setupBankUseCase.invoke(new SetupBankCommand(parseCurrency(currency)));
    }

    @And("exchange rate of {double} defined for {word}")
    public void addExchangeRate(double rate, String currency) {
        addExchangeRateUseCase.invoke(new AddExchangeRateCommand(rate, parseCurrency(currency)));
    }

    @Given("an existing portfolio containing")
    public void anExistingPortfolioContaining(DataTable moneys) {
        portfolioRepositoryFake.save(new Portfolio());
        moneys.asLists(String.class)
                .stream()
                .map(row -> new AddInPortfolio(parseDouble(row.get(0)), parseCurrency(row.get(1))))
                .forEach(addInPortfolioUseCase::invoke);
    }

    @When("they evaluate their portfolio in the given currency the result should be")
    public void evaluate(DataTable evaluations) {
        evaluations.asLists(String.class)
                .stream()
                .map(row -> Tuple.of(parseDouble(row.get(0)), parseCurrency(row.get(1))))
                .forEach(expectedResult -> {
                    var evaluationResult = evaluatePortfolioUseCase.invoke(new EvaluatePortfolio(expectedResult._2));
                    assertThat(evaluationResult)
                            .hasRightValueSatisfying(received ->
                                    assertClosedTo(received, new Money(expectedResult._1, expectedResult._2))
                            );
                });
    }

    private void assertClosedTo(EvaluationResult evaluationResult, Money expected) {
        Assertions.assertThat(evaluationResult.amount()).isCloseTo(expected.getAmount(), offset(0.001d));
        Assertions.assertThat(evaluationResult.currency()).isEqualTo(expected.getCurrency());
    }

    private Currency parseCurrency(String currency) {
        return of(Currency.values())
                .find(c -> c.toString().equals(currency))
                .get();
    }
}