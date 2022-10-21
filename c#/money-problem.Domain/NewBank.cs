using LanguageExt;

namespace money_problem.Domain;

public class NewBank
{
    public static NewBank WithPivotCurrency(Currency currency)
    {
        return new NewBank();
    }

    public Either<Error,NewBank> Add(NewExchangeRate exchangeRate)
    {
        return Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency."));
    }
}