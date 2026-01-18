package com.davidparker.dms.e2e.tests;

import com.davidparker.dms.e2e.base.BaseE2ETest;
import com.davidparker.dms.e2e.pages.ComplianceDashboardPage;
import com.davidparker.dms.e2e.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Compliance E2E Tests")
public class ComplianceE2ETest extends BaseE2ETest {
    
    private void loginAsAdmin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo();
        loginPage.login("admin@example.com", "admin123");
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }
    
    @Test
    @DisplayName("Admin can access compliance dashboard")
    public void testAccessComplianceDashboard() {
        loginAsAdmin();
        
        ComplianceDashboardPage compliancePage = new ComplianceDashboardPage(driver);
        compliancePage.navigateTo();
        
        assertThat(driver.getCurrentUrl()).contains("/compliance");
    }
    
    @Test
    @DisplayName("Admin can view PCI compliance report")
    public void testViewPciReport() {
        loginAsAdmin();
        
        ComplianceDashboardPage compliancePage = new ComplianceDashboardPage(driver);
        compliancePage.navigateTo();
        compliancePage.clickPciReportTab();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".pci-report, [aria-label*='PCI'], table")));
    }
    
    @Test
    @DisplayName("Admin can view GDPR compliance section")
    public void testViewGdprSection() {
        loginAsAdmin();
        
        ComplianceDashboardPage compliancePage = new ComplianceDashboardPage(driver);
        compliancePage.navigateTo();
        compliancePage.clickGdprTab();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".gdpr-section, [aria-label*='GDPR']")));
    }
    
    @Test
    @DisplayName("Admin can view ISO 27001 controls")
    public void testViewIso27001Controls() {
        loginAsAdmin();
        
        ComplianceDashboardPage compliancePage = new ComplianceDashboardPage(driver);
        compliancePage.navigateTo();
        compliancePage.clickIso27001Tab();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".iso-controls, [aria-label*='ISO'], table")));
    }
    
    @Test
    @DisplayName("Admin can export data subject data")
    public void testExportDataSubjectData() {
        loginAsAdmin();
        
        ComplianceDashboardPage compliancePage = new ComplianceDashboardPage(driver);
        compliancePage.navigateTo();
        compliancePage.clickGdprTab();
        compliancePage.clickExportData();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form, input[name='dataSubjectId'], [aria-label*='Export']")));
    }
    
    @Test
    @DisplayName("Admin can request data erasure")
    public void testRequestDataErasure() {
        loginAsAdmin();
        
        ComplianceDashboardPage compliancePage = new ComplianceDashboardPage(driver);
        compliancePage.navigateTo();
        compliancePage.clickGdprTab();
        compliancePage.clickErasureRequest();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form, input[name='dataSubjectId'], [aria-label*='Erasure']")));
    }
}
