package com.davidparker.dms.e2e.pages;

import com.davidparker.dms.e2e.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentListPage extends BasePage {
    
    private static final By UPLOAD_BUTTON = By.cssSelector("button[aria-label='Upload new document']");
    private static final By CLASSIFICATION_FILTER = By.id("classification-filter");
    private static final By DOCUMENT_TABLE = By.cssSelector("table.document-table");
    private static final By DOCUMENT_ROWS = By.cssSelector("table.document-table tbody tr");
    private static final By DOCUMENT_NAME_COLUMN = By.cssSelector("td:first-child");
    private static final By VIEW_BUTTON = By.cssSelector("button[aria-label*='View document']");
    private static final By DOWNLOAD_BUTTON = By.cssSelector("button[aria-label*='Download document']");
    private static final By LOADING_INDICATOR = By.cssSelector(".loading");
    private static final By ERROR_MESSAGE = By.cssSelector(".error");
    private static final By EMPTY_STATE = By.cssSelector(".empty-state");
    
    public DocumentListPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateTo() {
        driver.get(TestConfig.getBaseUrl() + "/documents");
        waitForPageLoad();
    }
    
    public void waitForPageLoad() {
        waitForElementToDisappear(LOADING_INDICATOR);
    }
    
    public void clickUploadButton() {
        click(UPLOAD_BUTTON);
    }
    
    public void selectClassificationFilter(String classification) {
        WebElement filter = findElement(CLASSIFICATION_FILTER);
        filter.click();
        filter.findElement(By.xpath(".//option[text()='" + classification + "']")).click();
    }
    
    public int getDocumentCount() {
        if (isElementPresent(EMPTY_STATE)) {
            return 0;
        }
        return findElements(DOCUMENT_ROWS).size();
    }
    
    public List<String> getDocumentNames() {
        return findElements(DOCUMENT_ROWS).stream()
            .map(row -> row.findElement(DOCUMENT_NAME_COLUMN).getText())
            .collect(Collectors.toList());
    }
    
    public void clickViewDocument(String documentName) {
        List<WebElement> rows = findElements(DOCUMENT_ROWS);
        for (WebElement row : rows) {
            if (row.findElement(DOCUMENT_NAME_COLUMN).getText().equals(documentName)) {
                row.findElement(VIEW_BUTTON).click();
                break;
            }
        }
    }
    
    public void clickDownloadDocument(String documentName) {
        List<WebElement> rows = findElements(DOCUMENT_ROWS);
        for (WebElement row : rows) {
            if (row.findElement(DOCUMENT_NAME_COLUMN).getText().equals(documentName)) {
                row.findElement(DOWNLOAD_BUTTON).click();
                break;
            }
        }
    }
    
    public boolean isErrorDisplayed() {
        return isElementVisible(ERROR_MESSAGE);
    }
    
    public String getErrorMessage() {
        if (isErrorDisplayed()) {
            return getText(ERROR_MESSAGE);
        }
        return "";
    }
    
    public boolean isEmptyStateDisplayed() {
        return isElementVisible(EMPTY_STATE);
    }
}
