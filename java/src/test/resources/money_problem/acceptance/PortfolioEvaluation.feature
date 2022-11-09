Feature: Portfolio evaluation
  Customers can evaluate their Portfolio in the 3 currencies supported by our system.

  Background:
    Given our Bank system with EUR as Pivot Currency
    And exchange rate of 1.2 defined for USD
    And exchange rate of 1344 defined for KRW

  Scenario: Evaluate in EUR
    Given an existing portfolio
    And our customer adds 5678.89 USD on their portfolio
    And our customer adds 5674567.245 KRW on their portfolio
    And our customer adds 9432 USD on their portfolio
    And our customer adds 4989.67 EUR on their portfolio
    When they evaluate their portfolio in EUR the amount should be 21804.227
