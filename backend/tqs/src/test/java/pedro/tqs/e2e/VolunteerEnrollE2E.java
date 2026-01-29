package pedro.tqs.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class VolunteerEnrollE2E extends E2EBase {

    @Test
    void register_login_enroll_showsPendingParticipation() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));

        String email = "vol_" + UUID.randomUUID() + "@test.com";
        String pass = "strongPass1";

        // Register
        driver.get(baseUrl + "/register");
        driver.findElement(By.cssSelector("[data-testid='register-name']")).sendKeys("Vol");
        driver.findElement(By.cssSelector("[data-testid='register-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='register-password']")).sendKeys(pass);
        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();

        // Login
        driver.get(baseUrl + "/login");
        driver.findElement(By.cssSelector("[data-testid='login-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys(pass);
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();

        // Browse opportunities and enroll in first card
        driver.get(baseUrl + "/");
        WebElement enrollBtn = wait.until(d ->
                d.findElement(By.cssSelector("[data-testid='opportunity-card'] [data-testid='enroll-btn']"))
        );
        enrollBtn.click();

        // My participations: should contain at least one PENDING
        driver.get(baseUrl + "/participations");
        WebElement row = wait.until(d ->
                d.findElement(By.cssSelector("[data-testid='participation-row']"))
        );
        String status = row.findElement(By.cssSelector("[data-testid='participation-status']")).getText().trim();

        assertEquals("PENDING", status);
    }
}
