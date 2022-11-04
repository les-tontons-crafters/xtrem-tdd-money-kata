package money_problem.usecases.add_exchange_rate;

import money_problem.domain.Currency;

public record AddExchangeRate(double rate, Currency currency) {
}
