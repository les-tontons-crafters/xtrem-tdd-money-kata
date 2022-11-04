package money_problem.usecases.evaluate_portfolio;

import money_problem.domain.Currency;

import java.time.LocalDateTime;

public record EvaluationResult(LocalDateTime evaluatedAt, double amount, Currency currency) {
    
}
