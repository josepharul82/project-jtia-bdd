# 🔄 Step Definitions

## Conventions
- **Nom de fichier** : `[Feature]Step.java` (ex: `LoginFlowStep.java`)
- **Package** : `fr.axa.automation.step`
- **Héritage** : Toujours étendre `AbstractStep`
- **Annotations** : Utiliser `@Getter` et `@FieldDefaults(level = AccessLevel.PROTECTED)` de Lombok
- **WebDriver** : Récupérer via `Hook.webDriver`
- **Annotations Cucumber** : `@Given`, `@When`, `@Then`, `@And`

## Bonnes Pratiques
- **Logging** : Utiliser `info("message")` pour tracer les étapes
- **Screenshots** : Appeler `screenshot()` aux moments clés
- **Gestion des Erreurs** : Propager les exceptions avec `throws Exception`
- **Organisation** : Regrouper les steps liés dans le même fichier

## Exemple Complet
```java
package fr.axa.automation.step;

import fr.axa.automation.model.MyPage;
import com.axa.webengine.step.AbstractStep;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class MyFlowStep extends AbstractStep {
    
    WebDriver driver;
    MyPage myPage;
    
    public MyFlowStep() throws Exception {
        driver = Hook.webDriver;
        myPage = new MyPage(driver);
    }
    
    @Given("I am on the page {string}")
    public void iAmOnPage(String url) throws Exception {
        info("Navigating to: " + url);
        driver.get(url);
        screenshot();
    }
    
    @When("I enter {string} in the field")
    public void iEnterInField(String value) throws Exception {
        info("Entering value: " + value);
        myPage.myInput.sendKeys(value);
        screenshot();
    }
    
    @When("I click the submit button")
    public void iClickSubmit() throws Exception {
        info("Clicking submit button");
        myPage.submitButton.click();
        myPage.sync(2); // Attendre 2 secondes
        screenshot();
    }
    
    @Then("I should see the error message {string}")
    public void iShouldSeeError(String expectedMessage) throws Exception {
        info("Validating error message");
        myPage.errorMessage.assertContentText(expectedMessage);
        screenshot();
    }
}
```

## Cycle de Vie et Synchronisation
- Utiliser `page.sync(seconds)` pour attendre explicitement
- Préférer les attentes explicites aux `Thread.sleep()`
- Ajouter des screenshots après chaque action importante
- Logger chaque étape avec `info()` pour faciliter le débogage

