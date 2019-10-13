Feature: Transfer Money Concurrently between bank accounts

  Background:
    Given an Account with initial Balance of 1000 as firstAccount
    And an Account with initial Balance of 1500 as secondAccount

  Scenario:
    When In parallel, 1000 transfers are launched from firstAccount to secondAccount for 1

    Then account firstAccount should have a balance of 0
    And account secondAccount should have a balance of 2500