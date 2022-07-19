using System;
using FsCheck;
using money_problem.Domain;

namespace money_problem.Tests;

public class MoneyGenerator
{
    private const double MaxAMount = 1000000000;
    private const int MaxDigits = 5;

    public static Arbitrary<Money> GenerateMoneys() =>
        Arb.From(from amount in GetAmountGenerator()
            from currency in Arb.Generate<Currency>()
            select new Money(amount, currency));

    private static Gen<double> GetAmountGenerator() => Arb.Default.Float()
        .MapFilter(x => Math.Round(x, MaxDigits), x => x is >= 0 - MaxAMount and <= MaxAMount).Generator;
}