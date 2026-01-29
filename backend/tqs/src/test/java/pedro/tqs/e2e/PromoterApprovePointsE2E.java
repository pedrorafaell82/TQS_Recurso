package pedro.tqs.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PromoterApprovePointsE2E extends E2EBase {

    @Test
    void promoterApproves_participation_awardsPoints() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));

        String email = "vol_" + UUID.randomUUID() + "@test.com";
        String pass = "strongPass1";

        // Register + login as volunteer
        driver.get(baseUrl + "/register");
        driver.findElement(By.cssSelector("[data-testid='register-name']")).sendKeys("Vol");
        driver.findElement(By.cssSelector("[data-testid='register-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='register-password']")).sendKeys(pass);
        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();

        driver.get(baseUrl + "/login");
        driver.findElement(By.cssSelector("[data-testid='login-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys(pass);
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();

        // Enroll
        driver.get(baseUrl + "/");
        WebElement enrollBtn = wait.until(d ->
                d.findElement(By.cssSelector("[data-testid='opportunity-card'] [data-testid='enroll-btn']"))
        );
        enrollBtn.click();

        // Capture participationId from My participations
        driver.get(baseUrl + "/participations");
        WebElement row = wait.until(d -> d.findElement(By.cssSelector("[data-testid='participation-row']")));
        String pid = row.findElement(By.cssSelector("[data-testid='participation-id']")).getText().trim();
        assertFalse(pid.isBlank());

        // Login as promoter
        driver.get(baseUrl + "/login");
        driver.findElement(By.cssSelector("[data-testid='login-email']")).sendKeys("promoter@test.com");
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys("strongPass1");
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();

        // Approve participation by id
        driver.get(baseUrl + "/promoter");
        driver.findElement(By.cssSelector("[data-testid='approve-participation-id']")).sendKeys(pid);
        driver.findElement(By.cssSelector("[data-testid='approve-btn']")).click();

        // Login back as volunteer and check points balance > 0
        driver.get(baseUrl + "/login");
        driver.findElement(By.cssSelector("[data-testid='login-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys(pass);
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();

        driver.get(baseUrl + "/points");
        WebElement bal = wait.until(d -> d.findElement(By.cssSelector("[data-testid='points-balance']")));
        int balance = Integer.parseInt(bal.getText().trim());

        assertTrue(balance > 0);
    }
}
