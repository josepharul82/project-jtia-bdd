# Architecture du Projet


## Fichiers Importants

- **Git Ignore** : `target/`, `*.class`, `*.jar`, `.idea/`, `*.iml`, `.run/`
- **Rapports** : `target/report-gherkin/` et `target/cucumber-report/`
- **Pages HTML** : `src/main/resources/html/`
- **Features Gherkin** : `src/main/resources/features/`
- **Configuration** : `src/main/resources/application.yml`


## Structure des Packages
```
    └── *Step.java             # Implémentation des steps Gherkin
    ├── Hook.java              # Before/After hooks
└── step/                       # Step Definitions Cucumber
│   └── *Page.java             # Classes représentant les pages web
├── model/                      # Page Object Models (POM)
├── CucumberRunnerTest.java    # Point d'entrée des tests Cucumber
fr.axa.automation/
```
## Stack Technique

- **Rapports** : HTML, JSON, JUnit XML
- **Build Tool** : Maven
- **Automatisation Web** : Selenium WebDriver avec WebEngine
- **Framework de Test** : Cucumber avec JUnit Platform
- **Language** : Java


