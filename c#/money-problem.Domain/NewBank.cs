using LanguageExt;

namespace money_problem.Domain;

public class NewBank
{
    private readonly Currency pivotCurrency;

    private NewBank(Currency pivotCurrency) => this.pivotCurrency = pivotCurrency;

    public static NewBank WithPivotCurrency(Currency currency) => new(currency);

    public Either<Error, NewBank> Add(NewExchangeRate exchangeRate) =>
        this.IsPivotCurrency(exchangeRate.Currency)
            ? Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency."))
            : Either<Error, NewBank>.Right(this);

    private bool IsPivotCurrency(Currency currency) => currency == this.pivotCurrency;
}