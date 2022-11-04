package money_problem.usecases.setup_bank;

import money_problem.domain.Currency;
import money_problem.usecases.common.Request;

public record SetupBankCommand(Currency currency) implements Request {
}
