package com.davidparker.dms.e2e.tests;

import com.davidparker.dms.e2e.base.BaseE2ETest;
import com.davidparker.dms.e2e.config.TestConfig;
import com.davidparker.dms.e2e.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Authentication E2E Tests")
public class AuthenticationE2ETest extends BaseE2ETest {
    
    @Test
    @DisplayName("User can successfully login with valid credentials")
    public void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo();
        
        loginPage.login("test@example.com", "password123");
        
        // Wait for redirect to dashboard
        wait.until(d -> d.getCurrentUrl().contains("/documents") || 
                        d.getCurrentUrl().contains("/dashboard"));
        
        assertThat(driver.getCurrentUrl()).doesNotContain("/login");
    }
    
    @Test
    @DisplayName("User cannot login with invalid credentials")
    public void testFailedLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo();
        
        loginPage.login("invalid@example.com", "wrongpassword");
        
        assertThat(loginPage.isErrorMessageDisplayed()).isTrue();
        assertThat(loginPage.getErrorMessage()).isNotEmpty();
    }
    
    @Test
    @DisplayName("User cannot login with empty credentials")
    public void testLoginWithEmptyCredentials() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo();
        
        loginPage.enterEmail("");
        loginPage.enterPassword("");
        loginPage.clickLogin();
        
        // Form validation should prevent submission or show error
        assertThat(loginPage.isErrorMessageDisplayed() || 
                  driver.getCurrentUrl().contains("/login")).isTrue();
    }
    
    @Test
    @DisplayName("User is redirected to login when accessing protected pages without authentication")
    public void testUnauthenticatedAccess() {
        driver.get(TestConfig.getBaseUrl() + "/documents");
        
        // Should redirect to login
        wait.until(d -> d.getCurrentUrl().contains("/login"));
        assertThat(driver.getCurrentUrl()).contains("/login");
    }
}
