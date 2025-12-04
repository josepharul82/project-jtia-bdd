# 🪝 Hooks et Cycle de Vie

## Configuration des Hooks

### Before Hook
- **Initialisation** : `WebdriverHelper.initializeWebDriver()`
- **Moment** : Avant chaque scénario Cucumber
- **Usage** : Initialiser le WebDriver et la configuration

### After Hook
- **Nettoyage** : `WebdriverHelper.quitWebDriver(webDriver)`
- **Moment** : Après chaque scénario Cucumber
- **Usage** : Fermer le navigateur et libérer les ressources

### Partage du WebDriver
- Le WebDriver est stocké dans `Hook.webDriver` (static)
- Partagé entre toutes les step definitions
- Accessible via `Hook.webDriver` dans les constructeurs de steps

## Exemple de Hook
```java
package fr.axa.automation.step;

import com.axa.webengine.helper.WebdriverHelper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;

public class Hook {
    
    public static WebDriver webDriver;
    
    @Before
    public void setUp() throws Exception {
        webDriver = WebdriverHelper.initializeWebDriver();
    }
    
    @After
    public void tearDown() {
        if (webDriver != null) {
            WebdriverHelper.quitWebDriver(webDriver);
        }
    }
}
```

## Tags Conditionnels
Vous pouvez définir des hooks conditionnels avec des tags :

```java
@Before("@browser")
public void setUpBrowser() {
    // Initialisation spécifique pour les tests navigateur
}

@Before("@mobile")
public void setUpMobile() {
    // Initialisation spécifique pour les tests mobile
}

@After("@cleanup")
public void specialCleanup() {
    // Nettoyage spécial pour certains scénarios
}
```

## Bonnes Pratiques
- Toujours nettoyer les ressources dans le After hook
- Gérer les exceptions dans les hooks
- Logger les actions importantes des hooks
- Capturer des screenshots en cas d'échec dans le After hook

