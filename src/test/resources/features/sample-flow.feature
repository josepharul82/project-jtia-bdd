Feature: insurance flow test

  Background:
    Given I visit the test page "https://axafrance.github.io/webengine-dotnet/demo/home-insurance/"


  @flow
  Scenario: First scenario - Login and search prospect by name
    When I fill the login form with username "test"
    And I the login form with password "test"
    And I click on the next button
    Then I see the prospect page
    And I should see the prospect page title "Prospect"
    When I search for prospect by name "test"
    And I click on the search button
    Then I should see the prospect ID "82918723"
    And I should see the info message containing "The customerId of customer"
    When I click on the next step button
