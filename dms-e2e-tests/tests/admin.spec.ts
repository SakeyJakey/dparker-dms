import { test, expect } from '@playwright/test';

test.describe('Admin Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin');
  });

  test('should display admin dashboard', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Administration Dashboard');
  });

  test('should show stat cards', async ({ page }) => {
    await expect(page.locator('.stat-cards')).toBeVisible();
    await expect(page.locator('text=Users')).toBeVisible();
    await expect(page.locator('text=Roles')).toBeVisible();
    await expect(page.locator('text=Applications')).toBeVisible();
  });

  test('should have navigation links to management pages', async ({ page }) => {
    await expect(page.locator('[aria-label="User management"]')).toBeVisible();
    await expect(page.locator('[aria-label="Role management"]')).toBeVisible();
    await expect(page.locator('[aria-label="Application management"]')).toBeVisible();
    await expect(page.locator('[aria-label="Audit logs"]')).toBeVisible();
  });

  test('should navigate to user management', async ({ page }) => {
    await page.click('[aria-label="User management"]');
    await expect(page).toHaveURL(/\/admin\/users/);
    await expect(page.locator('h1')).toContainText('User Management');
  });

  test('should navigate to role management', async ({ page }) => {
    await page.click('[aria-label="Role management"]');
    await expect(page).toHaveURL(/\/admin\/roles/);
    await expect(page.locator('h1')).toContainText('Role Management');
  });

  test('should navigate to application management', async ({ page }) => {
    await page.click('[aria-label="Application management"]');
    await expect(page).toHaveURL(/\/admin\/applications/);
    await expect(page.locator('h1')).toContainText('Application Management');
  });
});

test.describe('User Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin/users');
  });

  test('should display user management page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('User Management');
  });

  test('should have add user button', async ({ page }) => {
    await expect(page.locator('text=+ Add User')).toBeVisible();
  });

  test('should toggle create user form', async ({ page }) => {
    await page.click('text=+ Add User');
    await expect(page.locator('#username')).toBeVisible();
    await expect(page.locator('#email')).toBeVisible();
    await expect(page.locator('#displayName')).toBeVisible();
    // Click again to hide
    await page.click('text=Cancel');
    await expect(page.locator('#username')).not.toBeVisible();
  });
});

test.describe('Role Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin/roles');
  });

  test('should display role management page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Role Management');
  });

  test('should have add role button', async ({ page }) => {
    await expect(page.locator('text=+ Add Role')).toBeVisible();
  });

  test('should toggle create role form', async ({ page }) => {
    await page.click('text=+ Add Role');
    await expect(page.locator('#roleName')).toBeVisible();
    await expect(page.locator('#roleDesc')).toBeVisible();
  });
});

test.describe('Application Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin/applications');
  });

  test('should display application management page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Application Management');
  });

  test('should have provision button', async ({ page }) => {
    await expect(page.locator('text=+ Provision Application')).toBeVisible();
  });

  test('should toggle provision form', async ({ page }) => {
    await page.click('text=+ Provision Application');
    await expect(page.locator('#entraId')).toBeVisible();
    await expect(page.locator('#appName')).toBeVisible();
  });
});
