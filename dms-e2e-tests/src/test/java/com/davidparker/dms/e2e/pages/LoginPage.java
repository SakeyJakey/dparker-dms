package com.davidparker.dms.e2e.pages;

import com.davidparker.dms.e2e.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message");
    
    public LoginPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateTo() {
        driver.get(TestConfig.getBaseUrl() + "/login");
    }
    
    public void enterEmail(String email) {
        type(EMAIL_INPUT, email);
    }
    
    public void enterPassword(String password) {
        type(PASSWORD_INPUT, password);
    }
    
    public void clickLogin() {
        click(LOGIN_BUTTON);
    }
    
    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }
    
    public boolean isErrorMessageDisplayed() {
        return isElementVisible(ERROR_MESSAGE);
    }
    
    public String getErrorMessage() {
        if (isErrorMessageDisplayed()) {
            return getText(ERROR_MESSAGE);
        }
        return "";
    }
}
