import { test, expect } from '@playwright/test';

test.describe('AI Document Query', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/llm');
  });

  test('should display LLM query page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('AI Document Query');
  });

  test('should have query input', async ({ page }) => {
    await expect(page.locator('#queryInput')).toBeVisible();
  });

  test('should have mode selection', async ({ page }) => {
    await expect(page.locator('text=General Query')).toBeVisible();
    await expect(page.locator('text=Compliance Check')).toBeVisible();
  });

  test('should disable search button when query is empty', async ({ page }) => {
    const button = page.locator('button:has-text("Search")');
    await expect(button).toBeDisabled();
  });

  test('should enable search button when query is entered', async ({ page }) => {
    await page.fill('#queryInput', 'Find documents about compliance');
    const button = page.locator('button:has-text("Search")');
    await expect(button).toBeEnabled();
  });

  test('should switch between query modes', async ({ page }) => {
    // Default is general query
    const generalRadio = page.locator('input[value="query"]');
    await expect(generalRadio).toBeChecked();

    // Switch to compliance check
    await page.click('text=Compliance Check');
    const complianceRadio = page.locator('input[value="compliance"]');
    await expect(complianceRadio).toBeChecked();
  });
});
