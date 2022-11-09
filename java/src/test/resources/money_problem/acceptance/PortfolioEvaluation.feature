Feature: Portfolio evaluation
  Customers can evaluate their Portfolio in the 3 currencies supported by our system.

  Background:
    Given our Bank system with EUR as Pivot Currency
    And exchange rate of 1.2 defined for USD
    And exchange rate of 1344 defined for KRW

  Scenario: Evaluate in supported currencies
    Given an existing portfolio containing
      | 5678.89     | USD |
      | 5674567.245 | KRW |
      | 9432        | USD |
      | 4989.67     | EUR |
    When they evaluate their portfolio in the given currency the result should be
      | 21804.227            | EUR |
      | 26165.072            | USD |
      | 2.9304880525000002E7 | KRW |