# What is TDD
TDD is a technique borne of a set of beliefs about code :

* Simplicity - the art of maximizing the amount of work *not* done
* Obviousness and clarity are more virtuous than cleverness
* Writing uncluttered code is a key component of being successful

`Test-Driven Development is a way of managing fear during programming - Kant Beck`

## Designing and structuring code
* TDD is not fundamentally about testing code
* Its purpose : `improve the design and structure of the code`
    * The Unit Tests that we end up with are an added bonus
    * Primary benefit : simplicity of design we get

## A bias toward simplicity
* In software, we can measure simplicity :
    * Fewer lines of code per feature
    * Lower Cyclomatic Complexity
    * Fewer side effects
    * Smaller runtime / memory requirements
* TDD forces us to craft the simplest thing that works
* Virtue isn't mystical :
    * Using TDD won't cut by half :
        * your development time
        * the lines of code
        * defect count
    * It will allow you to arrest the temptation to introduce artificial / contrived complexity

## Increased Confidence
TDD increases our confidence in our code :

* Each new test flexes the system in new and previously untested ways
* Over time : the tests suite guards us against regression failures

![Confidence](img/confidence.png)

## Building block of TDD
A 3-phase process :

* ***Red*** : We write a failing test
    * Including possible compilation failures
    * We run the test suite to verify the failing test
* ***Green*** : We write **just enough production code** to make the test green
    * We run the test suite to verify this
* ***Refactor*** : We remove any code smells
    * Duplication, hardcoded values, improper use of language idioms, ...
    * If we break any test during this phase :
        * Prioritize getting back to green before exiting this phase

[![TDD steps](img/tdd.png)](https://tddmanifesto.com/getting-started/)

## 3 Rules
1. Write production code only to pass a failing unit test.
1. Write no more of a unit test than sufficient to fail (compilation failures are failures).
1. Write no more production code than necessary to pass the one failing unit test.

![TDD cycle](img/tdd-rules.png)

# Xtrem TDD
Your craft mentor available online [here](https://xtrem-tdd.netlify.app/)

![Confidence](img/xtrem.png)

# A word on Pair Programming
Change role at each new failing test.
![Ping Pong pairing](img/ping-pong-pairing.jpg)