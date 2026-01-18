package com.davidparker.dms.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ComplianceDashboardPage extends BasePage {
    
    private static final By PCI_REPORT_TAB = By.cssSelector("a[aria-label='PCI Report']");
    private static final By GDPR_TAB = By.cssSelector("a[aria-label='GDPR']");
    private static final By ISO27001_TAB = By.cssSelector("a[aria-label='ISO 27001']");
    private static final By EXPORT_DATA_BUTTON = By.cssSelector("button[aria-label='Export data']");
    private static final By ERASURE_REQUEST_BUTTON = By.cssSelector("button[aria-label='Request erasure']");
    
    public ComplianceDashboardPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateTo() {
        driver.get(TestConfig.getBaseUrl() + "/compliance");
    }
    
    public void clickPciReportTab() {
        click(PCI_REPORT_TAB);
    }
    
    public void clickGdprTab() {
        click(GDPR_TAB);
    }
    
    public void clickIso27001Tab() {
        click(ISO27001_TAB);
    }
    
    public void clickExportData() {
        click(EXPORT_DATA_BUTTON);
    }
    
    public void clickErasureRequest() {
        click(ERASURE_REQUEST_BUTTON);
    }
}
