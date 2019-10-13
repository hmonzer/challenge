Feature: Test Money Transfer Api

  Background:
    Given an Account with initial Balance of 1000 as firstAccount
    And an Account with initial Balance of 1500 as secondAccount

  Scenario:
    When a money transfer is launched from firstAccount to secondAccount for 100

    Then account firstAccount should have a balance of 900
    And account secondAccount should have a balance of 1600
    And transfer request should have status COMPLETED

  Scenario:
    When a money transfer is launched from firstAccount to secondAccount for 9000

    Then account firstAccount should have a balance of 1000
    And account secondAccount should have a balance of 1500
    And transfer request should have status INSUFFICIENT_FUNDS