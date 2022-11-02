package money_problem.acceptance;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class PortfolioEvaluationStepDefinitions {
    @Given("our Bank system with EUR as Pivot Currency")
    public void ourBankSystemWithEURAsPivotCurrency() {
    }

    @And("exchange rate of {double} defined for USD")
    public void exchangeRateOfDefinedForUSD(int arg0, int arg1) {
    }

    @And("exchange rate of {int} defined for KRW")
    public void exchangeRateOfDefinedForKRW(int arg0) {
    }

    @Given("an existing customer")
    public void anExistingCustomer() {
    }

    @And("he\\/she adds {double} USD on his\\/her portfolio")
    public void heSheAddsUSDOnHisHerPortfolio(int arg0, int arg1) {
    }

    @And("he\\/she adds {double} KRW on his\\/her portfolio")
    public void heSheAddsKRWOnHisHerPortfolio(int arg0, int arg1) {
    }

    @And("he\\/she adds {int} USD on his\\/her portfolio")
    public void heSheAddsUSDOnHisHerPortfolio(int arg0) {
    }

    @And("he\\/she adds {double} EUR on his\\/her portfolio")
    public void heSheAddsEUROnHisHerPortfolio(int arg0, int arg1) {
    }

    @When("he\\/she evaluates his\\/her portfolio in EUR the result should be {double} EUR")
    public void heSheEvaluatesHisHerPortfolioInEURTheResultShouldBeEUR(int arg0, int arg1) {
    }
}
