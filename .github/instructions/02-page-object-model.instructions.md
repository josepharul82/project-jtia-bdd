# 📄 Page Object Model (POM)

## Conventions
- **Nom de fichier** : `[PageName]Page.java` (ex: `LoginStepPage.java`)
- **Package** : `fr.axa.automation.model`
- **Héritage** : Toujours étendre `AbstractPageModel`
- **Annotations** : Utiliser `@FieldDefaults(level = AccessLevel.PUBLIC)` de Lombok

## Éléments Web
- Déclarer avec `WebElementDescription.builder()`
- Utiliser des sélecteurs explicites : `.tagName()`, `.name()`, `.id()`, `.xPath()`, `.cssSelector()`
- **Préférer `name` et `id` au xPath quand c'est possible**

## Exemple Complet
```java
package fr.axa.automation.model;

import com.axa.webengine.core.web.element.WebElementDescription;
import com.axa.webengine.model.AbstractPageModel;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class MyPage extends AbstractPageModel {
    
    // Input avec id et name
    WebElementDescription myInput = WebElementDescription.builder()
        .tagName("input")
        .name("myInput")
        .id("my-input")
        .build();
    
    // Bouton avec id unique
    WebElementDescription submitButton = WebElementDescription.builder()
        .tagName("button")
        .id("submit-btn")
        .build();
    
    // Élément avec sélecteur CSS
    WebElementDescription errorMessage = WebElementDescription.builder()
        .cssSelector(".alert-danger")
        .build();
    
    public MyPage(WebDriver webDriver) throws Exception {
        populateDriver(webDriver);
    }
}
```

## Bonnes Pratiques
- Un fichier Page par page web testée
- Nommer les éléments de façon descriptive
- Grouper les éléments par zone fonctionnelle (header, form, footer, etc.)
- Documenter les éléments complexes avec des commentaires

