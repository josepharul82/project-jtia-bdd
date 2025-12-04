# 📚 Ressources et Documentation

## Documentation Externe

### Frameworks et Outils
- **Framework Webengine** : https://axafrance.github.io/webengine-dotnet/
  - Guide complet du framework d'automatisation AXA
  - API Reference et exemples
  
- **Cucumber Java** : https://cucumber.io/docs/cucumber/
  - Syntaxe Gherkin
  - Step Definitions
  - Hooks et configuration
  
- **Selenium WebDriver** : https://www.selenium.dev/documentation/
  - Locators et sélecteurs
  - Interactions avec les éléments
  - Attentes et synchronisation
  
- **Lombok** : https://projectlombok.org/
  - Annotations
  - Configuration
  - Features

### Outils de Développement
- **Maven** : https://maven.apache.org/guides/
- **JUnit** : https://junit.org/junit5/docs/current/user-guide/
- **IntelliJ IDEA** : https://www.jetbrains.com/help/idea/

---

## Rapports et Outputs

### Rapports HTML
- **Emplacement** : `target/report-gherkin/` et `target/cucumber-report/`
- **Contenu** : Résultats détaillés des tests avec screenshots
- **Génération** : Automatique après chaque exécution

### Ouvrir les Rapports
```bash
# Windows
start target/report-gherkin/index.html
```

### Screenshots
- **Emplacement** : Inclus dans les rapports HTML
- **Format** : PNG
- **Génération** : Via la méthode `screenshot()` dans les steps

---

## Templates et Exemples

### Template Page Object
```java
@FieldDefaults(level = AccessLevel.PUBLIC)
public class MyPage extends AbstractPageModel {
    WebElementDescription element = WebElementDescription.builder()
        .tagName("tag")
        .id("element-id")
        .build();
    
    public MyPage(WebDriver webDriver) throws Exception {
        populateDriver(webDriver);
    }
}
```

### Template Step Definition
```java
@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class MyStep extends AbstractStep {
    WebDriver driver;
    MyPage myPage;
    
    public MyStep() throws Exception {
        driver = Hook.webDriver;
        myPage = new MyPage(driver);
    }
    
    @When("I do something")
    public void doSomething() throws Exception {
        info("Doing something");
        screenshot();
    }
}
```

### Template Feature
```gherkin
Feature: My Feature

  Background:
    Given I visit the page "url"

  @flow
  Scenario: My Scenario
    When I do something
    Then I should see the result
```

---

## Aide et Support

### Problèmes Courants

#### Le WebDriver ne démarre pas
- Vérifier la configuration dans `application.yml`
- Vérifier que le driver est dans le PATH
- Vérifier les capabilities

#### Les éléments ne sont pas trouvés
- Vérifier les sélecteurs (ID, name, xpath)
- Ajouter des attentes explicites lorsque nécessaire
- Vérifier que la page est complètement chargée

#### Les tests sont instables
- Augmenter les timeouts
- Ajouter des attentes explicites lorsque nécessaire
- Vérifier les sélecteurs (préférer ID/name)

#### Les rapports ne se génèrent pas
- Vérifier les plugins Maven dans `pom.xml`
- Vérifier les permissions sur le dossier `target/`
- Exécuter `mvn clean` avant les tests

---

## Glossaire

- **POM (Page Object Model)** : Pattern de conception pour l'automatisation des tests
- **Step Definition** : Implémentation Java d'une étape Gherkin
- **Hook** : Méthode exécutée avant/après les scénarios
- **Feature** : Fichier Gherkin décrivant une fonctionnalité
- **Scenario** : Cas de test dans une feature
- **Background** : Préconditions communes à tous les scénarios
- **Tag** : Étiquette pour catégoriser et filtrer les tests
- **Assertion** : Vérification d'une condition attendue
- **Screenshot** : Capture d'écran prise pendant le test
- **Selector** : Moyen d'identifier un élément web (ID, XPath, CSS)

---

## Contacts et Contribution

### Mise à Jour de la Documentation
- Cette documentation doit être mise à jour régulièrement
- Ajouter des exemples lorsque de nouvelles patterns sont utilisés
- Documenter les décisions d'architecture importantes

### Contribution
- Créer une branche pour les modifications
- Suivre les conventions de code établies
- Ajouter des tests pour les nouvelles fonctionnalités
- Mettre à jour la documentation

---


