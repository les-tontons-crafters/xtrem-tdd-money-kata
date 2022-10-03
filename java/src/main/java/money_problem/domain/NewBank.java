package money_problem.domain;

import io.vavr.control.Either;

import static io.vavr.API.Left;

public class NewBank {
    public static NewBank withPivotCurrency(Currency pivotCurrency) {
        return new NewBank();
    }

    public Either<Error, NewBank> add(ExchangeRate exchangeRate) {
        return Left(new Error("Can not add an exchange rate for the pivot currency"));
    }
}
