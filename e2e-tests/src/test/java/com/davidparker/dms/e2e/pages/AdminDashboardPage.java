package com.davidparker.dms.e2e.pages;

import com.davidparker.dms.e2e.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminDashboardPage extends BasePage {
    
    private static final By USERS_TAB = By.cssSelector("a[aria-label='Users']");
    private static final By ROLES_TAB = By.cssSelector("a[aria-label='Roles']");
    private static final By PERMISSIONS_TAB = By.cssSelector("a[aria-label='Permissions']");
    private static final By APPLICATIONS_TAB = By.cssSelector("a[aria-label='Applications']");
    private static final By CREATE_USER_BUTTON = By.cssSelector("button[aria-label='Create user']");
    private static final By CREATE_ROLE_BUTTON = By.cssSelector("button[aria-label='Create role']");
    private static final By CREATE_PERMISSION_BUTTON = By.cssSelector("button[aria-label='Create permission']");
    private static final By PROVISION_APP_BUTTON = By.cssSelector("button[aria-label='Provision application']");
    
    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateTo() {
        driver.get(TestConfig.getBaseUrl() + "/admin");
    }
    
    public void clickUsersTab() {
        click(USERS_TAB);
    }
    
    public void clickRolesTab() {
        click(ROLES_TAB);
    }
    
    public void clickPermissionsTab() {
        click(PERMISSIONS_TAB);
    }
    
    public void clickApplicationsTab() {
        click(APPLICATIONS_TAB);
    }
    
    public void clickCreateUser() {
        click(CREATE_USER_BUTTON);
    }
    
    public void clickCreateRole() {
        click(CREATE_ROLE_BUTTON);
    }
    
    public void clickCreatePermission() {
        click(CREATE_PERMISSION_BUTTON);
    }
    
    public void clickProvisionApplication() {
        click(PROVISION_APP_BUTTON);
    }
}
