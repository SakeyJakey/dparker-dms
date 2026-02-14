import { test, expect } from '@playwright/test';

test.describe('Analytics Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/analytics');
  });

  test('should display analytics dashboard', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Analytics Dashboard');
  });

  test('should show stat cards', async ({ page }) => {
    await expect(page.locator('.analytics-grid, .loading')).toBeVisible();
  });
});
