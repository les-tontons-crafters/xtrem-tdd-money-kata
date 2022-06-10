using System.Collections.Immutable;

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

    private static bool ContainsFailure(IEnumerable<ConversionResult<string>> results) =>
        results.Any(result => result.IsFailure());

    private List<ConversionResult<string>> GetConvertedMoneys(Bank bank, Currency currency) =>
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

    public ConversionResult<string> Evaluate(Bank bank, Currency currency)
    {
        var results = this.GetConvertedMoneys(bank, currency);
        return ContainsFailure(results)
            ? ConversionResult<string>.FromFailure(this.ToFailure(results))
            : ConversionResult<string>.FromMoney(this.ToSuccess(results, currency));
    }

    private string ToFailure(IEnumerable<ConversionResult<string>> results) =>
        $"Missing exchange rate(s): {GetMissingRates(results.Where(result => result.IsFailure()).Select(result => result.Failure!))}";

    private Money ToSuccess(IEnumerable<ConversionResult<string>> results, Currency currency) =>
        new(results.Where(result => result.IsSuccess()).Sum(result => result.Money!.Amount), currency);
}