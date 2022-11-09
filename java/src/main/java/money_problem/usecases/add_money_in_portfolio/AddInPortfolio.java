package money_problem.usecases.add_money_in_portfolio;

import money_problem.domain.Currency;
import money_problem.usecases.common.Request;

public record AddInPortfolio(double amount, Currency currency) implements Request {
}
