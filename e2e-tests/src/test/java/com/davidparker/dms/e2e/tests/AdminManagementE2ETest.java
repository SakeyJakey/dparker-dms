package com.davidparker.dms.e2e.tests;

import com.davidparker.dms.e2e.base.BaseE2ETest;
import com.davidparker.dms.e2e.pages.AdminDashboardPage;
import com.davidparker.dms.e2e.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Admin Management E2E Tests")
public class AdminManagementE2ETest extends BaseE2ETest {
    
    private void loginAsAdmin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo();
        loginPage.login("admin@example.com", "admin123");
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }
    
    @Test
    @DisplayName("Admin can access admin dashboard")
    public void testAccessAdminDashboard() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        
        assertThat(driver.getCurrentUrl()).contains("/admin");
    }
    
    @Test
    @DisplayName("Admin can navigate to Users tab")
    public void testNavigateToUsersTab() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        adminPage.clickUsersTab();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("table, .user-list, [aria-label*='user']")));
    }
    
    @Test
    @DisplayName("Admin can navigate to Roles tab")
    public void testNavigateToRolesTab() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        adminPage.clickRolesTab();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("table, .role-list, [aria-label*='role']")));
    }
    
    @Test
    @DisplayName("Admin can navigate to Permissions tab")
    public void testNavigateToPermissionsTab() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        adminPage.clickPermissionsTab();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("table, .permission-list, [aria-label*='permission']")));
    }
    
    @Test
    @DisplayName("Admin can navigate to Applications tab")
    public void testNavigateToApplicationsTab() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        adminPage.clickApplicationsTab();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("table, .application-list, [aria-label*='application']")));
    }
    
    @Test
    @DisplayName("Admin can create a new user")
    public void testCreateUser() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        adminPage.clickUsersTab();
        adminPage.clickCreateUser();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form, [aria-label*='Create user'], input[name='email']")));
    }
    
    @Test
    @DisplayName("Admin can create a new role")
    public void testCreateRole() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        adminPage.clickRolesTab();
        adminPage.clickCreateRole();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form, [aria-label*='Create role'], input[name='name']")));
    }
    
    @Test
    @DisplayName("Admin can provision a new application")
    public void testProvisionApplication() {
        loginAsAdmin();
        
        AdminDashboardPage adminPage = new AdminDashboardPage(driver);
        adminPage.navigateTo();
        adminPage.clickApplicationsTab();
        adminPage.clickProvisionApplication();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form, [aria-label*='Provision'], input[name='name']")));
    }
}
