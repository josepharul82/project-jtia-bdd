Feature: insurance flow test

  Background:
    Given I visit the test page "https://axafrance.github.io/webengine-dotnet/demo/home-insurance/"


  @flow
  Scenario: First scenario - Login and search prospect by name
    When I fill the login form with username "test"
    And I the login form with password "test"
    And I click on the next button
    Then I see the prospect page



