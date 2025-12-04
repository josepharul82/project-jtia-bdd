package fr.axa.automation.step;

import fr.axa.automation.webengine.helper.WebdriverHelper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;

public class Hook {

    public static WebDriver webDriver;

    @Before
    public void setUp() throws Exception {
        System.setProperty("wdm.edgeDriverUrl", "https://msedgedriver.microsoft.com/");
        System.out.println("This will run before the Scenario");
        webDriver = WebdriverHelper.initializeWebDriver();
    }

    @After
    public void afterScenario()  throws Exception {
        WebdriverHelper.quitWebDriver(webDriver);
    }
}