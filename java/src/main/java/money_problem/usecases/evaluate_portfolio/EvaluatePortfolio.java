package money_problem.usecases.evaluate_portfolio;

import money_problem.domain.Currency;
import money_problem.usecases.common.Request;

public record EvaluatePortfolio(Currency currency) implements Request {

}
