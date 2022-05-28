namespace money_problem.Domain
{
    public sealed class MissingExchangeRateException : Exception
    {
        public MissingExchangeRateException(Currency from, Currency to)
            : base($"{from}->{to}")
        {
        }
    }
}