package fr.axa.automation.step;


import fr.axa.automation.model.LoginModelPage;
import fr.axa.automation.webengine.step.AbstractStep;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.WebDriver;


@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class LoginFlowStep extends AbstractStep {

    WebDriver webDriver;
    LoginModelPage loginModelPage;

    public LoginFlowStep() throws Exception {
        webDriver = Hook.webDriver;
        loginModelPage = new LoginModelPage(webDriver);
    }

    @Given("I visit the test page {string}")
    public void fillTheLoginPage(String url) throws InterruptedException {
        info("Open test website page");
        webDriver.get(url);
        loginModelPage.maximize();
        screenshot();
    }

    @When("I fill the login form with username {string}")
    public void fillLogin(String userName) throws Exception {
        info("Fill login");
        loginModelPage.login.sendKeys(userName);
    }

    @When("I the login form with password {string}")
    public void fillPassword(String password)  throws Exception{
        info("Fill password");
        loginModelPage.password.sendKeys(password);
    }

    @When("I click on the next button")
    public void clickNextButton() throws Exception{
        info("Fill password");
        loginModelPage.submitButton.click();
    }

    @Then("I see the prospect page")
    public void seeTheProspectPage()throws Exception{
        loginModelPage.sync(5);
        loginModelPage.labelProspectPage.assertContentText("Home Insurance - Prospect");
        screenshot();
    }

}
