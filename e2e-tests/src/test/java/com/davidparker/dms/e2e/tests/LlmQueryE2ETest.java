package com.davidparker.dms.e2e.tests;

import com.davidparker.dms.e2e.base.BaseE2ETest;
import com.davidparker.dms.e2e.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LLM Query E2E Tests")
public class LlmQueryE2ETest extends BaseE2ETest {
    
    private void loginAsUser() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo();
        loginPage.login("test@example.com", "password123");
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }
    
    @Test
    @DisplayName("User can access LLM query interface")
    public void testAccessLlmQueryInterface() {
        loginAsUser();
        
        navigateTo("/llm/query");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("textarea, input[type='text'], [aria-label*='query']")));
    }
    
    @Test
    @DisplayName("User can submit a document query")
    public void testSubmitDocumentQuery() {
        loginAsUser();
        
        navigateTo("/llm/query");
        
        By queryInput = By.cssSelector("textarea, input[type='text'], [aria-label*='query']");
        wait.until(ExpectedConditions.presenceOfElementLocated(queryInput));
        
        driver.findElement(queryInput).sendKeys("Find all documents related to compliance");
        
        By submitButton = By.cssSelector("button[type='submit'], button[aria-label*='Submit'], button[aria-label*='Query']");
        if (driver.findElements(submitButton).size() > 0) {
            driver.findElement(submitButton).click();
            
            // Wait for results
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".query-results, .llm-response, [aria-label*='result']")));
        }
    }
    
    @Test
    @DisplayName("User can view query results")
    public void testViewQueryResults() {
        loginAsUser();
        
        navigateTo("/llm/query");
        
        By queryInput = By.cssSelector("textarea, input[type='text'], [aria-label*='query']");
        wait.until(ExpectedConditions.presenceOfElementLocated(queryInput));
        
        driver.findElement(queryInput).sendKeys("What documents contain sensitive information?");
        
        By submitButton = By.cssSelector("button[type='submit'], button[aria-label*='Submit']");
        if (driver.findElements(submitButton).size() > 0) {
            driver.findElement(submitButton).click();
            
            // Verify results are displayed
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".query-results")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".llm-response")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[aria-label*='result']"))
            ));
        }
    }
    
    @Test
    @DisplayName("User cannot submit empty query")
    public void testEmptyQueryValidation() {
        loginAsUser();
        
        navigateTo("/llm/query");
        
        By queryInput = By.cssSelector("textarea, input[type='text'], [aria-label*='query']");
        wait.until(ExpectedConditions.presenceOfElementLocated(queryInput));
        
        By submitButton = By.cssSelector("button[type='submit'], button[aria-label*='Submit']");
        if (driver.findElements(submitButton).size() > 0) {
            driver.findElement(submitButton).click();
            
            // Should show validation error or prevent submission
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error, .validation-error")),
                ExpectedConditions.alertIsPresent()
            ));
        }
    }
}
