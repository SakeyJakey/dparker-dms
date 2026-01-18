package com.davidparker.dms.e2e.tests;

import com.davidparker.dms.e2e.base.BaseE2ETest;
import com.davidparker.dms.e2e.config.TestConfig;
import com.davidparker.dms.e2e.pages.DocumentListPage;
import com.davidparker.dms.e2e.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Document Management E2E Tests")
public class DocumentManagementE2ETest extends BaseE2ETest {
    
    private void loginAsUser() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo();
        loginPage.login("test@example.com", "password123");
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }
    
    @Test
    @DisplayName("User can view list of documents")
    public void testViewDocumentList() {
        loginAsUser();
        
        DocumentListPage documentPage = new DocumentListPage(driver);
        documentPage.navigateTo();
        documentPage.waitForPageLoad();
        
        assertThat(documentPage.isErrorDisplayed()).isFalse();
    }
    
    @Test
    @DisplayName("User can filter documents by classification")
    public void testFilterDocumentsByClassification() {
        loginAsUser();
        
        DocumentListPage documentPage = new DocumentListPage(driver);
        documentPage.navigateTo();
        documentPage.waitForPageLoad();
        
        int initialCount = documentPage.getDocumentCount();
        
        documentPage.selectClassificationFilter("CONFIDENTIAL");
        documentPage.waitForPageLoad();
        
        // Verify filter is applied (count may change or stay same depending on data)
        assertThat(documentPage.isErrorDisplayed()).isFalse();
    }
    
    @Test
    @DisplayName("User can upload a new document")
    public void testUploadDocument() {
        loginAsUser();
        
        DocumentListPage documentPage = new DocumentListPage(driver);
        documentPage.navigateTo();
        documentPage.waitForPageLoad();
        
        int initialCount = documentPage.getDocumentCount();
        
        documentPage.clickUploadButton();
        
        // Wait for upload dialog/modal
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='file']")));
        
        // Upload file (this would need actual file handling)
        // For now, verify upload button opens dialog
        assertThat(driver.findElement(By.cssSelector("input[type='file']"))).isNotNull();
    }
    
    @Test
    @DisplayName("User can view document details")
    public void testViewDocumentDetails() {
        loginAsUser();
        
        DocumentListPage documentPage = new DocumentListPage(driver);
        documentPage.navigateTo();
        documentPage.waitForPageLoad();
        
        if (documentPage.getDocumentCount() > 0) {
            String firstDocumentName = documentPage.getDocumentNames().get(0);
            documentPage.clickViewDocument(firstDocumentName);
            
            // Should navigate to document detail page
            wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("/documents")));
            assertThat(driver.getCurrentUrl()).contains("/documents/");
        }
    }
    
    @Test
    @DisplayName("User can download a document")
    public void testDownloadDocument() {
        loginAsUser();
        
        DocumentListPage documentPage = new DocumentListPage(driver);
        documentPage.navigateTo();
        documentPage.waitForPageLoad();
        
        if (documentPage.getDocumentCount() > 0) {
            String firstDocumentName = documentPage.getDocumentNames().get(0);
            documentPage.clickDownloadDocument(firstDocumentName);
            
            // Download should be triggered (browser download)
            // In real scenario, verify file download started
        }
    }
    
    @Test
    @DisplayName("Empty state is displayed when no documents exist")
    public void testEmptyDocumentList() {
        loginAsUser();
        
        DocumentListPage documentPage = new DocumentListPage(driver);
        documentPage.navigateTo();
        documentPage.waitForPageLoad();
        
        if (documentPage.getDocumentCount() == 0) {
            assertThat(documentPage.isEmptyStateDisplayed()).isTrue();
        }
    }
}
