package com.davidparker.dms.e2e.base;

import com.davidparker.dms.e2e.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BaseE2ETest {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    @BeforeEach
    public void setUp() {
        driver = TestConfig.createWebDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }
    
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    protected void navigateTo(String path) {
        driver.get(TestConfig.getBaseUrl() + path);
    }
}
