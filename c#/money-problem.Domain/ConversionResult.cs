namespace money_problem.Domain;

public class ConversionResult<T>
{
    public ConversionResult(Money money)
    {
        this.Money = money;
    }

    public ConversionResult(T failure)
    {
        this.Failure = failure;
    }

    public Money? Money { get; }

    public T? Failure { get; }

    public bool IsFailure() => this.Failure is { };

    public bool IsSuccess() => this.Money is { };

    public static ConversionResult<T> FromFailure(T failure) => new ConversionResult<T>(failure);

    public static ConversionResult<T> FromMoney(Money money) => new ConversionResult<T>(money);
}