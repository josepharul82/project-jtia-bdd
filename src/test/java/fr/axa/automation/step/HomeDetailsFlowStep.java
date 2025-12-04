package fr.axa.automation.step;


import fr.axa.automation.model.HomeDetailsModelPage;
import fr.axa.automation.webengine.step.AbstractStep;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;


@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class HomeDetailsFlowStep extends AbstractStep {

    WebDriver driver;
    HomeDetailsModelPage homeDetailsPage;


    public HomeDetailsFlowStep() throws Exception {
        driver = Hook.webDriver;
        homeDetailsPage = new HomeDetailsModelPage(driver);
    }

    // Home Location Steps
    @When("I fill the street number with {string}")
    public void fillStreetNumber(String streetNumber) throws Exception {
        info("Fill street number: " + streetNumber);
        homeDetailsPage.streetNumber.sendKeys(streetNumber);
        screenshot();
    }

    @When("I fill the street name with {string}")
    public void fillStreetName(String streetName) throws Exception {
        info("Fill street name: " + streetName);
        homeDetailsPage.streetName.sendKeys(streetName);
        screenshot();
    }

    @When("I fill the city with {string}")
    public void fillCity(String city) throws Exception {
        info("Fill city: " + city);
        homeDetailsPage.city.sendKeys(city);
        screenshot();
    }

    @When("I fill the postcode with {string}")
    public void fillPostcode(String postcode) throws Exception {
        info("Fill postcode: " + postcode);
        homeDetailsPage.postcode.sendKeys(postcode);
        screenshot();
    }

    @When("I fill the region with {string}")
    public void fillRegion(String region) throws Exception {
        info("Fill region: " + region);
        homeDetailsPage.region.sendKeys(region);
        screenshot();
    }

    @When("I select country {string}")
    public void selectCountry(String country) throws Exception {
        info("Select country: " + country);
        homeDetailsPage.country.selectByValue(country);
        screenshot();
    }

    // Home Type Steps
    @When("I select home type {string}")
    public void selectHomeType(String homeType) throws Exception {
        info("Select home type: " + homeType);
        if ("apartment".equalsIgnoreCase(homeType)) {
            homeDetailsPage.homeTypeApartment.click();
        } else if ("house".equalsIgnoreCase(homeType)) {
            homeDetailsPage.homeTypeHouse.click();
        }
        homeDetailsPage.sync(1);
        screenshot();
    }

    // House Characteristics Steps
    @When("I fill the number of floors with {string}")
    public void fillNumberOfFloors(String floors) throws Exception {
        info("Fill number of floors: " + floors);
        homeDetailsPage.floors.sendKeys(floors);
        screenshot();
    }

    @When("I fill the surface of backyard with {string}")
    public void fillSurfaceBackyard(String surface) throws Exception {
        info("Fill surface of backyard: " + surface);
        homeDetailsPage.surfaceBackyard.sendKeys(surface);
        screenshot();
    }

    @When("I select pool option {string}")
    public void selectPoolOption(String option) throws Exception {
        info("Select pool option: " + option);
        if ("yes".equalsIgnoreCase(option)) {
            homeDetailsPage.poolYes.click();
        } else if ("no".equalsIgnoreCase(option)) {
            homeDetailsPage.poolNo.click();
        }
        screenshot();
    }

    // Apartment Details Steps
    @When("I select total floors of building {string}")
    public void selectTotalFloors(String totalFloors) throws Exception {
        info("Select total floors: " + totalFloors);
        homeDetailsPage.totalFloors.selectByValue(totalFloors);
        screenshot();
    }

    @When("I fill my apartment floor with {string}")
    public void fillMyFloor(String floor) throws Exception {
        info("Fill apartment floor: " + floor);
        homeDetailsPage.myFloors.sendKeys(floor);
        screenshot();
    }

    @When("I select elevator option {string}")
    public void selectElevatorOption(String option) throws Exception {
        info("Select elevator option: " + option);
        if ("yes".equalsIgnoreCase(option)) {
            homeDetailsPage.elevatorYes.click();
        } else if ("no".equalsIgnoreCase(option)) {
            homeDetailsPage.elevatorNo.click();
        }
        screenshot();
    }

    // Common Steps
    @When("I select number of rooms {string}")
    public void selectNumberOfRooms(String rooms) throws Exception {
        info("Select number of rooms: " + rooms);
        homeDetailsPage.rooms.selectByValue(rooms);
        screenshot();
    }

    @When("I fill the surface with {string}")
    public void fillSurface(String surface) throws Exception {
        info("Fill surface: " + surface);
        homeDetailsPage.surface.sendKeys(surface);
        screenshot();
    }

    // Navigation
    @When("I click on the home details next button")
    public void clickNextButton() throws Exception {
        info("Click on next step button");
        homeDetailsPage.nextStepButton.click();
        homeDetailsPage.sync(2);
        screenshot();
    }

    // Verification
    @Then("I see the home details page")
    public void verifyHomeDetailsPage() throws Exception {
        info("Verify home details page is displayed");
        homeDetailsPage.pageTitle.assertContentText("Fill the home details");
        screenshot();
    }

    @Then("I should see the home details page title {string}")
    public void verifyHomeDetailsPageTitle(String expectedTitle) throws Exception {
        info("Verify home details page title: " + expectedTitle);
        homeDetailsPage.pageTitle.assertContentText(expectedTitle);
        screenshot();
    }
}

