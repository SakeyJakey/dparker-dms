import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('should load the application', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL(/\/documents/);
  });

  test('should have main navigation', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('nav[aria-label="Main navigation"]')).toBeVisible();
    await expect(page.locator('text=Documents')).toBeVisible();
    await expect(page.locator('text=AI Query')).toBeVisible();
    await expect(page.locator('text=Admin')).toBeVisible();
    await expect(page.locator('text=Compliance')).toBeVisible();
  });

  test('should navigate between pages', async ({ page }) => {
    await page.goto('/');

    // Documents
    await page.click('nav a:has-text("Documents")');
    await expect(page).toHaveURL(/\/documents/);

    // Admin
    await page.click('nav a:has-text("Admin")');
    await expect(page).toHaveURL(/\/admin/);

    // Compliance
    await page.click('nav a:has-text("Compliance")');
    await expect(page).toHaveURL(/\/compliance/);

    // AI Query
    await page.click('nav a:has-text("AI Query")');
    await expect(page).toHaveURL(/\/llm/);
  });

  test('should have DMS logo link to home', async ({ page }) => {
    await page.goto('/admin');
    await page.click('[aria-label="DMS Home"]');
    await expect(page).toHaveURL(/\/documents/);
  });

  test('should have footer', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('footer')).toBeVisible();
    await expect(page.locator('footer')).toContainText('Document Management System');
  });

  test('should redirect unknown routes to documents', async ({ page }) => {
    await page.goto('/nonexistent-page');
    await expect(page).toHaveURL(/\/documents/);
  });
});

test.describe('Accessibility', () => {
  test('should have skip link', async ({ page }) => {
    await page.goto('/');
    const skipLink = page.locator('a.skip-link');
    // Skip link should exist in the HTML
    await expect(skipLink).toHaveCount(1);
  });

  test('should have proper heading hierarchy on documents page', async ({ page }) => {
    await page.goto('/documents');
    const h1 = page.locator('h1');
    await expect(h1).toHaveCount(1);
    await expect(h1).toContainText('Documents');
  });

  test('should have proper heading hierarchy on admin page', async ({ page }) => {
    await page.goto('/admin');
    const h1 = page.locator('h1');
    await expect(h1).toHaveCount(1);
    await expect(h1).toContainText('Administration Dashboard');
  });

  test('should have ARIA labels on navigation', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('[role="navigation"][aria-label="Main navigation"]')).toBeVisible();
  });

  test('should have ARIA labels on main content', async ({ page }) => {
    await page.goto('/documents');
    await expect(page.locator('[role="main"]')).toBeVisible();
  });
});
