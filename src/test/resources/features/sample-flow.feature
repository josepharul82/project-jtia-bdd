Feature: insurance flow test

  Background:
    Given I visit the test page "https://axafrance.github.io/webengine-dotnet/demo/home-insurance/"


  @flow
  Scenario Outline: First scenario - Login and search prospect by name
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
    And I fill the street number with "<streetNumber>"
    And I fill the street name with "<streetName>"
    And I fill the city with "<city>"
    And I fill the postcode with "<postcode>"
    And I fill the region with "<region>"
    And I select country "<country>"
    And I select home type "<homeType>"
    And I select total floors of building "<totalFloors>"
    And I fill my apartment floor with "<myFloor>"
    And I select elevator option "<elevator>"
    And I select number of rooms "<rooms>"
    And I fill the surface with "<surface>"
    And I click on the home details next button

    Examples:
      | streetNumber | streetName                  | city   | postcode | region         | country | homeType  | totalFloors | myFloor | elevator | rooms | surface |
      | 123          | Avenue des Champs-Élysées   | Paris  | 75008    | Île-de-France  | fr      | apartment | 4to7        | 3       | yes      | 3     | 85      |
      | 45           | Rue Principale              | Monaco | 98000    | Monte-Carlo    | ma      | apartment | <3          | 1       | no       | 2     | 60      |


