# Mutation Testing

## Instructions

- Use dotnet command to install stryker
`dotnet tool install -g dotnet-stryker`

- Go inside Tests folder
`cd money-problem/Tests`

- Run stryker command
`dotnet stryker`

- Check the report inside StrykerOutput folder

![Stryker Report Location](img/StrykerReportLocation.png)

![Stryker Report](img/StrykerReport.png)

- Check remaining mutants

## MissingExchangeRateException

![Mutant](img/MutantMissingExchangeRateException.png)

Stryker was able to create a mutant as we don't verify the exception message. The processed replaced the exception message by an empty string and our test still passed.

It proves our test isn't reliable.
Testing the message does provide value as it contain business information.

![Code Fix](img/MutantMissingExchangeRateExceptionCodeFix.png)

When implemented, verify your test pass and run the stryker command again.

![Stryker Report Fixed](img/MutantMissingExchangeRateExceptionFixed.png)

## MoneyCalculator

![Mutant](img/MutantMoneyCalculator.png)

Stryker was able to create a mutant as we don't have any assertion in our test.

![Code Fix](img/MutantMoneyCalculatorCodeFix.png)

 When implemented, verify your test pass and run the stryker command again.

![Stryker Report Fixed](img/MutantMoneyCalculatorFixed.png)


## Mutants eradicated!

 Congratulations you have killed your first mutants !!!

![Mutant killer](../../docs/img/mutant-killer.png)

Why you should focus on good assertion?
- Take a look at the [Test Desiderata](https://kentbeck.github.io/TestDesiderata/)
