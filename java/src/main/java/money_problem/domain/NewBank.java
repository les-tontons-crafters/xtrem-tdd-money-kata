package money_problem.domain;

import io.vavr.control.Either;

import static io.vavr.API.Left;
import static io.vavr.API.Right;

public class NewBank {
    private final Currency pivotCurrency;

    private NewBank(Currency pivotCurrency) {
        this.pivotCurrency = pivotCurrency;
    }

    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank(pivotCurrency);
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return exchangeRate.getCurrency() != pivotCurrency
                ? Right(new NewBank(pivotCurrency))
                : Left(new Error("Can not add an exchange rate for the pivot currency"));
    }

    public Either<Error, Money> convert(Money money, Currency to) {
        return convertFromAndToPivotCurrency(money, to)
                ? Right(money)
                : Left(new Error("No exchange rate defined for " + money.currency() + "->" + to));
    }

    private boolean convertFromAndToPivotCurrency(Money money, Currency to) {
        return money.currency() == to && to == pivotCurrency;
    }
}
