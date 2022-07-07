using System.Collections.Immutable;
using LanguageExt;

namespace money_problem.Domain;

public class Portfolio
{
    private readonly ICollection<Money> moneys;

    public Portfolio()
    {
        this.moneys = new List<Money>();
    }

    private Portfolio(IEnumerable<Money> moneys)
    {
        this.moneys = moneys.ToImmutableList();
    }

    private static bool ContainsFailure(IEnumerable<Either<string, Money>> results) =>
        results.Any(result => result.IsLeft);

    private List<Either<string, Money>> GetConvertedMoneys(Bank bank, Currency currency) =>
        this.moneys
            .Select(money => bank.Convert(money, currency))
            .ToList();

    public Portfolio Add(Money money)
    {
        List<Money> updatedMoneys = this.moneys.ToList();
        updatedMoneys.Add(money);
        return new Portfolio(updatedMoneys);
    }

    private static string GetMissingRates(IEnumerable<string> missingRates) => missingRates
        .Select(value => $"[{value}]")
        .Aggregate((r1, r2) => $"{r1},{r2}");

    private string ToFailure(IEnumerable<Either<string, Money>> results) =>
        $"Missing exchange rate(s): {GetMissingRates(results.Where(result => result.IsLeft).Select(result => result.IfRight(string.Empty)))}";

    private Money ToSuccess(IEnumerable<Either<string, Money>> results, Currency currency) =>
        new(results.Where(result => result.IsRight).Sum(result => result.IfLeft(Money.Empty(currency)).Amount), currency);

    public Either<string, Money> Evaluate(Bank bank, Currency currency)
    {
        var results = this.GetConvertedMoneys(bank, currency);
        return ContainsFailure(results)
            ? Either<string, Money>.Left(this.ToFailure(results))
            : Either<string, Money>.Right(this.ToSuccess(results, currency));
    }
}