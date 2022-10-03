## Redesign the Bank
Based on our new understanding / discoveries from the `example mapping` workshop we can work on the redesign of the `Bank`.
The examples will serve to drive our implementation.

If we increment on the existing `Bank` code, we will have directly huge impact on the whole system.

### Sprout Technique
One way to avoid it is to use a technique called [Sprout Technique](https://understandlegacycode.com/blog/key-points-of-working-effectively-with-legacy-code/#1-the-sprout-technique) from [Michael Feathers](https://wiki.c2.com/?MichaelFeathers) in his book [Working Effectively with Legacy Code](https://www.oreilly.com/library/view/working-effectively-with/0131177052/):
- Create your code somewhere else (use T.D.D to drive your implementation)
- Identify where you should call that code from the existing code: the insertion point.
- Call your code from the Existing or Legacy Code.

> Let's use this technique

### Tests list
Based on the example mapping outcome we can create a test list and think about the design of our `Bank`.

#### Define pivot currency
![Define Pivot Currency](img/bank-redesign-pivot-currency.png)

Regarding our business, those rules are really important and should be at the heart of our system.
To implement those, we can simply well encapsulate our class by making impossible `by design` to change the pivot currency of a living `Bank` instance.

We have not really test cases here, but it gives us ideas for preserving the integrity of our system.

#### Add an exchange rate

:red_circle: add a failing test on a new bank implementation



- From examples: edge cases
- ConvertThroughPivotCurrency
- PBT removed after we learned a lot ?
   - Check which property makes still sense
- [Parse don't validate](https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/)
- Continuation functions instead of procedural code
- Test improvement 
    - Parameterized Tests
    - Higher order function
    - Organize tests: failure vs success -> Nested classes
- Public contract of the bank:
    - add(ExchangeRate rate) -> ExchangeRate Currency + double
    - convert(Money money, Currency currency) -> Either<Error, Bank>
- Use Error types instead of String when using Either
- Call the branch -> 10-bank-redesign