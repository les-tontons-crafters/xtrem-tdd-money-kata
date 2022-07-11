using LanguageExt;

namespace money_problem.Domain;

public class Portfolio
{
    private readonly Seq<Money> moneys;

    public Portfolio() => this.moneys = Seq<Money>.Empty;

    private Portfolio(Seq<Money> moneys) => this.moneys = moneys;

    public Either<string, Money> Evaluate(Bank bank, Currency currency)
    {
        var results = this.GetConvertedMoneys(bank, currency);
        return ContainsFailure(results)
            ? Either<string, Money>.Left(this.ToFailure(results))
            : Either<string, Money>.Right(this.ToSuccess(results, currency));
    }

    private List<Either<string, Money>> GetConvertedMoneys(Bank bank, Currency currency) =>
        this.moneys
            .Select(money => bank.Convert(money, currency))
            .ToList();

    private static bool ContainsFailure(IEnumerable<Either<string, Money>> results) =>
        results.Any(result => result.IsLeft);

    private string ToFailure(IEnumerable<Either<string, Money>> results) =>
        $"Missing exchange rate(s): {GetMissingRates(results)}";

    private static string GetMissingRates(IEnumerable<Either<string, Money>> missingRates) => missingRates
        .Match(money => string.Empty, failure => $"[{failure}]")
        .Where(message => !string.IsNullOrEmpty(message))
        .Aggregate((r1, r2) => $"{r1},{r2}");

    private Money ToSuccess(IEnumerable<Either<string, Money>> results, Currency currency) =>
        new(results
                .Where(result => result.IsRight)
                .Sum(result => result.IfLeft(Money.Empty(currency)).Amount),
            currency);

    public Portfolio Add(Money money) => new(this.moneys.Add(money));
}