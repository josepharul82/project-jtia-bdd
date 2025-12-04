package fr.axa.automation.step;


import fr.axa.automation.model.ProspectModelPage;
import fr.axa.automation.webengine.step.AbstractStep;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;


@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class ProspectFlowStep extends AbstractStep {

    WebDriver driver;
    ProspectModelPage prospectPage;


    public ProspectFlowStep() throws Exception {
        driver = Hook.webDriver;
        prospectPage = new ProspectModelPage(driver);
    }

    @When("I search for prospect by name {string}")
    public void searchProspectByName(String customerName) throws Exception {
        info("Enter prospect name: " + customerName);
        prospectPage.prospectName.sendKeys(customerName);
        screenshot();
    }

    @When("I click on the search button")
    public void clickSearchButton() throws Exception {
        info("Click on search button");
        prospectPage.searchButton.click();
        prospectPage.sync(2);
        screenshot();
    }

    @When("I enter prospect ID {string}")
    public void enterProspectId(String prospectId) throws Exception {
        info("Enter prospect ID: " + prospectId);
        prospectPage.prospectId.sendKeys(prospectId);
        screenshot();
    }

    @Then("I should see the prospect ID {string}")
    public void verifyProspectId(String expectedId) throws Exception {
        info("Verify prospect ID is displayed: " + expectedId);
        String actualId = prospectPage.prospectId.getAttribute("value");
        if (!expectedId.equals(actualId)) {
            throw new AssertionError("Expected prospect ID: " + expectedId + " but got: " + actualId);
        }
        screenshot();
    }

    @Then("I should see the info message containing {string}")
    public void verifyInfoMessage(String expectedMessage) throws Exception {
        info("Verify info message contains: " + expectedMessage);
        String actualMessage = prospectPage.infoMessage.getText();
        if (!actualMessage.contains(expectedMessage)) {
            throw new AssertionError("Expected message to contain: " + expectedMessage + " but got: " + actualMessage);
        }
        screenshot();
    }

    @Then("I should see the error message {string}")
    public void verifyErrorMessage(String expectedError) throws Exception {
        info("Verify error message: " + expectedError);
        prospectPage.errorMessage.assertContentText(expectedError);
        screenshot();
    }

    @When("I click on the next step button")
    public void clickNextStepButton() throws Exception {
        info("Click on next step button");
        prospectPage.nextStepButton.click();
        screenshot();
    }

    @Then("I should see the prospect page title {string}")
    public void verifyProspectPageTitle(String expectedTitle) throws Exception {
        info("Verify prospect page title: " + expectedTitle);
        prospectPage.pageTitle.assertContentText(expectedTitle);
        screenshot();
    }

}

