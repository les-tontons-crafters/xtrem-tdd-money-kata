# Money Problem
This kata comes from [Saleem Siddiqui](https://www.linkedin.com/in/ssiddiqui/) 's book

[![img/Learning-Test-Driven-Development.jpg](img/Learning-Test-Driven-Development.jpg)](https://www.oreilly.com/library/view/learning-test-driven-development/9781098106461/)

## What's the problem ?
We have  to build a spreadsheet to manage money in more than one currency : perhaps to manage a stock portfolio ?

| Stock | Stock exchange | Shares | Share Price | Total |
|---|---|---|---|---|
| IBM | NASDAQ | 100 | 124 USD | 12400 USD |
| BMW | DAX | 400 | 75 EUR | 30000 EUR |
| Samsung | KSE | 300 | 68000 KRW | 20400000 KRW |

To build it, we'd need to do simple arithmetic operations on numbers :

```text
5 USD x 2 = 10 USD
4002 KRW / 4 = 1000.5 KRW

// convert
5 USD + 10 EUR = 17 USD
1 USD + 1100 KRW = 2200 KRW
```

## List of Features to implement
We have already started the implementation by using TDD. We have discovered examples that helped us drive our implementation.

```text
Money Calculator :
✅ 10 EUR x 2 = 20 EUR
✅ 4002 KRW / 4 = 1000.5 KRW
✅ 5 USD + 10 USD = 15 USD

Bank implementation :
✅ Determine exchange rate based on the currencies involved (from -> to)
✅ Improve the implementation of exchange rates
✅ Allow exchange rates to be modified
```

A few of them are not developed yet, it will be your mission during this kata :

- [ ] 5 USD + 10 EUR = 17 USD
- [ ] 1 USD + 1100 KRW = 2200 KRW

## Constraints
You will have to develop using the main constraints:

- Test-Driven Development
- Pair Programming

We will work in small iteration (20'), in each iteration we will :

- Share the constraint for the iteration - 1'
	- Let you discover it
- Implement it in your code - 14'
- Debriefing - 5'
	- How did you apply it?
	- What did you learn?
	- How could it be useful in your current code base?
	- Our solution
		- Keep your code or switch to our solution branch to move on

List of constraints we will cover:

- Mutation Testing
- Generate Code From Usage
- Only immutable types
- No primitive types
- No for loops
- No void returns
- No exception authorized
	