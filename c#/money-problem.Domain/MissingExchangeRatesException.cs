using System.Runtime.CompilerServices;

namespace money_problem.Domain;

public class MissingExchangeRatesException : Exception
{
    public MissingExchangeRatesException(List<MissingExchangeRateException> missingExchangeRates)
        : base($"Missing exchange rate(s): {GetMissingRates(missingExchangeRates)}")
    {
    }

    private static string GetMissingRates(List<MissingExchangeRateException> missingRates) => missingRates
        .Select(exception => $"[{exception.Message}]")
        .Aggregate((r1, r2) => $"{r1},{r2}");
}