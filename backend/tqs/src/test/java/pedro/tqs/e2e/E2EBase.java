package pedro.tqs.e2e;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class E2EBase {

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    void setupDriver() throws Exception {
        baseUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:5173");

        String gridUrl = System.getenv().getOrDefault("SELENIUM_URL", "http://localhost:4444");

        FirefoxOptions opts = new FirefoxOptions();
        opts.addArguments("-headless");
        opts.addArguments("--width=1280");
        opts.addArguments("--height=900");

        driver = new RemoteWebDriver(
            URI.create(gridUrl).toURL(),
            opts
        );
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }
}
