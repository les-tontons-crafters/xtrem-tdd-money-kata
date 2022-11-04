package money_problem.usecases.evaluate_portfolio;

import money_problem.domain.Currency;

import java.util.UUID;

public record EvaluatePortfolio(UUID customerId, Currency currency) {
}
