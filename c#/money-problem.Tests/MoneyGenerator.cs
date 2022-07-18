using FsCheck;
using money_problem.Domain;

namespace money_problem.Tests;

public class MoneyGenerator
{
    private const double MaxAMount = 1000000000;
    
    public static Arbitrary<Money> GenerateMoneys() =>
        Arb.From(from amount in Arb.Generate<double>()
            from currency in Arb.Generate<Currency>()
            where amount is >= 0 - MaxAMount and <= MaxAMount 
            select new Money(amount, currency));
}