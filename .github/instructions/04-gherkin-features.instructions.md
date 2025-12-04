# 🥒 Features Gherkin

## Conventions
- **Emplacement** : `src/main/resources/features/`
- **Syntaxe** : Gherkin standard (Given/When/Then/And)
- **Tags** : Utiliser `@flow`, `@smoke`, `@regression` selon le contexte
- **Background** : Définir les préconditions communes à tous les scénarios

## Structure d'une Feature
```gherkin
Feature: Nom de la fonctionnalité
  Description optionnelle de la fonctionnalité

  Background:
    Given I visit the test page "https://example.com"

  @flow @smoke
  Scenario: Mon premier scénario
    When I perform an action
    Then I should see the result
    
  @flow
  Scenario: Mon second scénario avec paramètres
    Given I am logged in as "user@example.com"
    When I navigate to "dashboard"
    And I click on "settings"
    Then I should see "User Settings"
```

## Tags Recommandés
- `@flow` : Test de flux métier complet
- `@smoke` : Test de fumée (tests critiques rapides)
- `@regression` : Test de régression
- `@wip` : Work in progress (en cours de développement)
- `@bug-XXX` : Test lié à un bug spécifique

Utiliser plusieurs tags pour catégoriser les scénarios et faciliter l'exécution ciblée via Maven.

## Bonnes Pratiques
- Écrire des scénarios en langage métier, pas technique
- Utiliser des noms de scénarios descriptifs
- Privilégier les scénarios courts et focalisés
- Réutiliser les steps entre différents scénarios
- Utiliser le Background pour les préconditions communes
- Ajouter des exemples avec Scenario Outline pour les tests paramétrés

## Exemple avec Scenario Outline
```gherkin
Feature: Validation de formulaire

  @flow
  Scenario Outline: Validation des champs obligatoires
    Given I am on the registration page
    When I enter "<name>" in the name field
    And I enter "<email>" in the email field
    And I click submit
    Then I should see "<message>"

    Examples:
      | name  | email              | message                    |
      |       | test@example.com   | Name is required           |
      | John  |                    | Email is required          |
      | John  | test@example.com   | Registration successful    |
```

