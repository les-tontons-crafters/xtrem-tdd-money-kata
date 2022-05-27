namespace money_problem.Domain
{
    public class MissingExchangeRateException : Exception
    {
        public MissingExchangeRateException(Currency from, Currency to)
            : base($"{from}->{to}")
        {
        }
    }
}