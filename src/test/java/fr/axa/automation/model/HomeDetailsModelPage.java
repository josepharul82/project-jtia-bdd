package fr.axa.automation.model;

import fr.axa.automation.webengine.core.AbstractPageModel;
import fr.axa.automation.webengine.core.WebElementDescription;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;


@FieldDefaults(level = AccessLevel.PUBLIC)
public class HomeDetailsModelPage extends AbstractPageModel {

    // Page title
    WebElementDescription pageTitle = WebElementDescription.builder().tagName("h1").className("h1 text-success").build();

    // Home Location fields
    WebElementDescription streetNumber = WebElementDescription.builder().tagName("input").name("streetNumber").build();
    WebElementDescription streetName = WebElementDescription.builder().tagName("input").name("streetName").build();
    WebElementDescription city = WebElementDescription.builder().tagName("input").name("city").build();
    WebElementDescription postcode = WebElementDescription.builder().tagName("input").name("postcode").build();
    WebElementDescription region = WebElementDescription.builder().tagName("input").name("region").build();
    WebElementDescription country = WebElementDescription.builder().tagName("select").name("country").build();

    // Home Type radio buttons
    WebElementDescription homeTypeApartment = WebElementDescription.builder().tagName("input").id("home-type-appt").name("homeType").build();
    WebElementDescription homeTypeHouse = WebElementDescription.builder().tagName("input").id("home-type-house").name("homeType").build();

    // House characteristics
    WebElementDescription floors = WebElementDescription.builder().tagName("input").name("floors").build();
    WebElementDescription surfaceBackyard = WebElementDescription.builder().tagName("input").name("surface-backyard").build();
    WebElementDescription poolYes = WebElementDescription.builder().tagName("input").id("pool-yes").name("pool").build();
    WebElementDescription poolNo = WebElementDescription.builder().tagName("input").id("pool-no").name("pool").build();

    // Apartment details
    WebElementDescription totalFloors = WebElementDescription.builder().tagName("select").name("total-floors").build();
    WebElementDescription myFloors = WebElementDescription.builder().tagName("input").name("my-floors").build();
    WebElementDescription elevatorYes = WebElementDescription.builder().tagName("input").id("elevator-yes").name("elevator").build();
    WebElementDescription elevatorNo = WebElementDescription.builder().tagName("input").id("elevator-no").name("elevator").build();

    // Common fields
    WebElementDescription rooms = WebElementDescription.builder().tagName("select").name("rooms").build();
    WebElementDescription surface = WebElementDescription.builder().tagName("input").name("surface").build();

    // Messages and navigation
    WebElementDescription infoMessage = WebElementDescription.builder().tagName("p").id("info").build();
    WebElementDescription errorMessage = WebElementDescription.builder().tagName("p").id("error").build();
    WebElementDescription nextStepButton = WebElementDescription.builder().tagName("button").innerText("Next Step").build();


    public HomeDetailsModelPage(WebDriver webDriver) throws Exception {
        populateDriver(webDriver);
    }
}

