package money_problem.usecases.evaluate_portfolio;

import money_problem.domain.Currency;

public record EvaluationResult(double amount, Currency currency) {
}