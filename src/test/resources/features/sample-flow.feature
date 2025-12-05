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
    Then I see the home details page
    When I fill the street number with "123"
    And I fill the street name with "Avenue des Champs-Élysées"
    And I fill the city with "Paris"
    And I fill the postcode with "75008"
    And I fill the region with "Île-de-France"
    And I select country "fr"
    And I select home type "apartment"
    And I select total floors of building "4to7"
    And I fill my apartment floor with "3"
    And I select elevator option "yes"
    And I select number of rooms "3"
    And I fill the surface with "85"
    And I click on the home details next button

