package money_problem.usecases.add_exchange_rate;

import money_problem.domain.Currency;
import money_problem.usecases.common.Request;

public record AddExchangeRateCommand(double rate, Currency currency) implements Request {
}
