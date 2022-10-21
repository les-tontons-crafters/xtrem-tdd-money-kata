using FsCheck;
using FsCheck.Xunit;
using LanguageExt;
using static LanguageExt.Prelude;
using money_problem.Domain;

namespace money_problem.Tests;

public class NewBankProperties
{
    public NewBankProperties()
    {
        
    }
    
    [Property]
    private Property CannotAddExchangeRateForThePivotCurrencyOfTheBank(double rate, Currency currency) =>
        (NewBank
            .WithPivotCurrency(currency)
            .Add(new NewExchangeRate(currency, rate)) == Either<Error, NewBank>.Left(new Error("Cannot add an exchange rate for the pivot currency.")))
            .ToProperty();
}