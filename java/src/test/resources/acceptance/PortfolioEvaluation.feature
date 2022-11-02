Feature: Portfolio evaluation
  Client can evaluate his/her Portfolio in the 3 currencies supported by our system.

  Background:
    Given our Bank system with EUR as Pivot Currency
    And exchange rate of 1.2 defined for USD
    And exchange rate of 1344 defined for KRW

  Scenario: Evaluate in EUR
    Given an existing customer
    And he/she adds 5678.89 USD on his/her portfolio
    And he/she adds 5674567.245 KRW on his/her portfolio
    And he/she adds 9432 USD on his/her portfolio
    And he/she adds 4989.67 EUR on his/her portfolio
    When he/she evaluates his/her portfolio in EUR the result should be 21804.23 EUR
