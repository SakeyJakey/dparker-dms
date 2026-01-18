package com.davidparker.dms.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class BasePage {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }
    
    protected WebElement findElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
    protected List<WebElement> findElements(By locator) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }
    
    protected WebElement findClickableElement(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    protected WebElement findVisibleElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    protected void click(By locator) {
        findClickableElement(locator).click();
    }
    
    protected void type(By locator, String text) {
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(text);
    }
    
    protected String getText(By locator) {
        return findVisibleElement(locator).getText();
    }
    
    protected boolean isElementPresent(By locator) {
        try {
            findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    protected boolean isElementVisible(By locator) {
        try {
            findVisibleElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    protected void waitForElementToDisappear(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
    
    protected void waitForTextToBePresent(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }
}
