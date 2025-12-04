# GitHub Copilot Instructions - Projet d'Automatisation de Tests

## 🎯 Objectif
Ce fichier guide GitHub Copilot pour l'assistance au développement, à la maintenance et à l'extension du projet d'automatisation de tests `project-jtia`.

---

## 📂 Documentation Organisée

La documentation complète est maintenant organisée en fichiers thématiques dans le dossier `instructions/` :

### 🏗️ Architecture & Concepts
- **[01-architecture.instructions.md](instructions/01-architecture.instructions.md)** - Stack technique, structure des packages et fichiers importants
- **[10-resources.instructions.md](instructions/10-resources.instructions.md)** - Documentation externe, glossaire et aide

### 🧪 Développement de Tests
- **[02-page-object-model.instructions.md](instructions/02-page-object-model.instructions.md)** - Conventions POM, éléments web et exemples
- **[03-step-definitions.instructions.md](instructions/03-step-definitions.instructions.md)** - Steps Cucumber, logging et screenshots
- **[04-gherkin-features.instructions.md](instructions/04-gherkin-features.instructions.md)** - Features, scénarios et tags
- **[05-hooks-lifecycle.instructions.md](instructions/05-hooks-lifecycle.instructions.md)** - Hooks Before/After et cycle de vie

### ⚙️ Configuration & Outils
- **[07-configuration.instructions.md](instructions/07-configuration.instructions.md)** - Configuration YAML, environnements et WebDriver
- **[08-maven-commands.instructions.md](instructions/08-maven-commands.instructions.md)** - Commandes Maven et résolution de problèmes

### ✅ Qualité
- **[09-best-practices.instructions.md](instructions/09-best-practices.instructions.md)** - Bonnes pratiques, sélecteurs, logging et checklist

---

## 🚀 Quick Start

### Créer un Nouveau Test Complet

1. **Feature Gherkin** → Voir [04-gherkin-features.instructions.md](instructions/04-gherkin-features.instructions.md)
   ```gherkin
   Feature: Ma fonctionnalité
     @flow
     Scenario: Mon scénario
       When I perform an action
       Then I should see the result
   ```

2. **Page Object** → Voir [02-page-object-model.instructions.md](instructions/02-page-object-model.instructions.md)
   ```java
   @FieldDefaults(level = AccessLevel.PUBLIC)
   public class MyPage extends AbstractPageModel {
       WebElementDescription element = WebElementDescription.builder()
           .tagName("button").id("submit-btn").build();
   }
   ```

3. **Step Definition** → Voir [03-step-definitions.instructions.md](instructions/03-step-definitions.instructions.md)
   ```java
   @Getter
   @FieldDefaults(level = AccessLevel.PROTECTED)
   public class MyStep extends AbstractStep {
       @When("I perform an action")
       public void performAction() throws Exception {
           info("Action performed");
           screenshot();
       }
   }
   ```

4. **Exécution** → Voir [08-maven-commands.instructions.md](instructions/08-maven-commands.instructions.md)
   ```bash
   mvn clean test -Dcucumber.filter.tags="@flow"
   ```

---

## 📋 Règles Essentielles

### ✅ À TOUJOURS FAIRE
- Utiliser `@FieldDefaults(level = AccessLevel.PUBLIC)` pour les Pages
- Utiliser `@Getter` et `@FieldDefaults(level = AccessLevel.PROTECTED)` pour les Steps
- Préférer les sélecteurs `id` et `name` au `xPath`
- Ajouter `info()` pour le logging et `screenshot()` aux moments clés
- Propager les exceptions avec `throws Exception`
- Ajouter `id` et `name` à tous les éléments HTML interactifs

### ❌ À ÉVITER
- `Thread.sleep()` → Utiliser `page.sync(seconds)` à la place
- XPath complexes → Préférer ID, name ou CSS selector
- Avaler les exceptions sans les logger
- Oublier les screenshots après les actions importantes
- Éléments HTML sans `id` ni `name`

---

## 📚 Ressources Rapides

### Documentation
- **Webengine** : https://axafrance.github.io/webengine-dotnet/
- **Cucumber** : https://cucumber.io/docs/cucumber/
- **Selenium** : https://www.selenium.dev/documentation/

### Commandes Utiles
```bash
mvn clean test                              # Tous les tests
mvn test -Dcucumber.filter.tags="@flow"    # Tests avec tag @flow
mvn clean compile -DskipTests               # Compilation seule
```

### Rapports
- **Emplacement** : `target/report-gherkin/` 
- **Screenshots** : Inclus automatiquement dans les rapports

---

## 📖 Pour Plus de Détails

Consultez le **[README.instructions.md](instructions/README.instructions.md)** du dossier instructions pour une navigation complète et des références rapides.

---

**Note** : Cette documentation est organisée pour faciliter la navigation. Chaque fichier dans `instructions/` est autonome et peut être consulté indépendamment selon le contexte de travail.

