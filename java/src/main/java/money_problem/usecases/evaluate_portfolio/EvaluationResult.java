package money_problem.usecases.evaluate_portfolio;

import money_problem.domain.Currency;

import static money_problem.domain.Currency.EUR;

public record EvaluationResult(double amount, Currency currency) {
    public static final EvaluationResult ZERO = new EvaluationResult(0, EUR);
}