package money_problem.usecases.add_in_portfolio;

import money_problem.domain.Currency;

import java.util.UUID;

public record AddInPortfolio(UUID customerId, double amount, Currency currency) {
}
