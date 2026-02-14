import { test, expect } from '@playwright/test';

test.describe('Webhook Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin/webhooks');
  });

  test('should display webhook management page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Webhook Management');
  });

  test('should have add webhook button', async ({ page }) => {
    await expect(page.locator('text=+ Add Webhook')).toBeVisible();
  });

  test('should toggle create form', async ({ page }) => {
    await page.click('text=+ Add Webhook');
    await expect(page.locator('#whName')).toBeVisible();
    await expect(page.locator('#whUrl')).toBeVisible();
    await expect(page.locator('#whEvents')).toBeVisible();
  });
});

test.describe('API Key Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin/api-keys');
  });

  test('should display API key management page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('API Key Management');
  });

  test('should have create key button', async ({ page }) => {
    await expect(page.locator('text=+ Create API Key')).toBeVisible();
  });

  test('should toggle create form', async ({ page }) => {
    await page.click('text=+ Create API Key');
    await expect(page.locator('#keyName')).toBeVisible();
    await expect(page.locator('#keyScopes')).toBeVisible();
  });
});
