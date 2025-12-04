package fr.axa.automation.model;

import fr.axa.automation.webengine.core.AbstractPageModel;
import fr.axa.automation.webengine.core.WebElementDescription;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;


@FieldDefaults(level = AccessLevel.PUBLIC)
public class LoginModelPage extends AbstractPageModel {

    WebElementDescription login = WebElementDescription.builder().tagName("input").name("login").build();
    WebElementDescription password = WebElementDescription.builder().tagName("input").name("password").build();
    WebElementDescription submitButton = WebElementDescription.builder().tagName("button").xPath("/html/body/div/div[4]/button[2]").build();
    WebElementDescription labelProspectPage = WebElementDescription.builder().tagName("a").xPath("/html/body/nav[2]/div/a").build();

    public LoginModelPage(WebDriver webDriver) throws Exception {
        populateDriver(webDriver);
    }
}
