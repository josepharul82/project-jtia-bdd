package fr.axa.automation.model;

import fr.axa.automation.webengine.core.AbstractPageModel;
import fr.axa.automation.webengine.core.WebElementDescription;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;


@FieldDefaults(level = AccessLevel.PUBLIC)
public class ProspectModelPage extends AbstractPageModel {

    WebElementDescription prospectId = WebElementDescription.builder().tagName("input").name("prospectId").id("prospectId").build();
    WebElementDescription prospectName = WebElementDescription.builder().tagName("input").name("prospectName").id("prospectName").build();
    WebElementDescription searchButton = WebElementDescription.builder().tagName("button").innerText("Search").build();
    WebElementDescription infoMessage = WebElementDescription.builder().tagName("p").id("info").build();
    WebElementDescription errorMessage = WebElementDescription.builder().tagName("p").id("error").build();
    WebElementDescription nextStepButton = WebElementDescription.builder().tagName("button").innerText("Next Step").build();
    WebElementDescription pageTitle = WebElementDescription.builder().tagName("h1").className("h1 text-success").build();


    public ProspectModelPage(WebDriver webDriver) throws Exception {
        populateDriver(webDriver);
    }
}

