# ⚙️ Configuration & Environnement

## Fichier de Configuration

### Emplacement
- **Fichier** : `src/main/resources/application.yml`

### Structure
```yaml
webengineConfiguration:
  # Configuration Selenium/Appium
  name: project-name
  platformName: WINDOWS
  browserName: chrome
  
application-configuration:
  # Variables d'application
  baseUrl: http://localhost:8080
  environment: local
  logLevel: INFO
```

## Sections de Configuration

### webengineConfiguration
Configuration pour Selenium WebDriver et Appium :
- **name** : Nom du projet d'automatisation
- **platformName** : Plateforme (WINDOWS, MAC, ANDROID, IOS)
- **browserName** : Type de navigateur (chrome, firefox, edge, safari)

### application-configuration
Configuration spécifique à l'application :
- **baseUrl** : URL de base de l'application à tester
- **environment** : Environnement (local, dev, staging, prod)
- **logLevel** : Niveau de log (DEBUG, INFO, WARN, ERROR)
- **users** : Données de test (utilisateurs, mots de passe, etc.)

## Exemple Complet
```yaml
webengineConfiguration:
  name: project-jtia-automation
  platformName: WINDOWS
  browserName: CHROME
  appiumConfiguration:
    gridConnection: https://hub-cloud.browserstack.com/wd/hub
    userName: XXXXXXX
    password: XXXXXXX
    localTesting:
      activate: false
      arguments:
        force: true
        forcelocal: true
        binarypath: C:\\BrowserStack\\BrowserStackLocal.exe
        localIdentifier: XXXXYYYY
    capabilities:
      desiredCapabilitiesMap:
        geoLocation: FR
        deviceName: Samsung Galaxy S20 Ultra
        osVersion: 10.0
        projectName: project-name
        buildName: project-build-001
        sessionName: Samsung
        local: true
        networkLogs: true
        localIdentifier: XXXXYYYY
application-configuration:
  values:
    url: https://www.google.com
    varAzure: Charlie
```

## Configuration du WebDriver

### Partage entre Steps
- Le driver est partagé via `Hook.webDriver` (static)
- Accessible dans toutes les step definitions
- Initialisé avant chaque scénario
- Fermé après chaque scénario

### Récupération dans les Steps
```java
public MyFlowStep() throws Exception {
    driver = Hook.webDriver;
    myPage = new MyPage(driver);
}
```

## BrowserStack / Selenium Grid

### Configuration pour BrowserStack
```yaml
webengineConfiguration:
  name: project-jtia-automation
  platformName: WINDOWS
  browserName: CHROME
  appiumConfiguration:
    gridConnection: https://hub-cloud.browserstack.com/wd/hub
    userName: XXXXXXX
    password: XXXXXXX
    localTesting:
      activate: false
      arguments:
        force: true
        forcelocal: true
        binarypath: C:\\BrowserStack\\BrowserStackLocal.exe
        localIdentifier: XXXXYYYY
    capabilities:
      desiredCapabilitiesMap:
        geoLocation: FR
        deviceName: Samsung Galaxy S20 Ultra
        osVersion: 10.0
        projectName: project-name
        buildName: project-build-001
        sessionName: Samsung
        local: true
        networkLogs: true
        localIdentifier: XXXXYYYY
```

## Bonnes Pratiques
- Ne jamais commiter de credentials dans Git
- Utiliser des variables d'environnement pour les données sensibles
- Documenter toutes les propriétés de configuration
- Valider la configuration au démarrage des tests
- Utiliser des valeurs par défaut raisonnables

