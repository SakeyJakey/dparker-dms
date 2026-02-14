import { test, expect } from '@playwright/test';

test.describe('Compliance Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/compliance');
  });

  test('should display compliance dashboard', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Compliance Dashboard');
  });

  test('should show PCI-DSS section', async ({ page }) => {
    await expect(page.locator('[aria-label="PCI-DSS compliance"]')).toBeVisible();
    await expect(page.locator('text=PCI-DSS')).toBeVisible();
  });

  test('should show GDPR section', async ({ page }) => {
    await expect(page.locator('[aria-label="GDPR compliance"]')).toBeVisible();
    await expect(page.locator('text=GDPR')).toBeVisible();
  });

  test('should show ISO 27001 section', async ({ page }) => {
    await expect(page.locator('[aria-label="ISO 27001 compliance"]')).toBeVisible();
    await expect(page.locator('text=ISO 27001')).toBeVisible();
  });

  test('should show audit logs section with link', async ({ page }) => {
    await expect(page.locator('[aria-label="Audit logs"]')).toBeVisible();
    await expect(page.locator('text=View Audit Logs →')).toBeVisible();
  });

  test('should have GDPR data subject input', async ({ page }) => {
    await expect(page.locator('#dataSubjectId')).toBeVisible();
    await expect(page.locator('text=Export Data')).toBeVisible();
    await expect(page.locator('text=Request Erasure')).toBeVisible();
  });

  test('should have PCI report period selector', async ({ page }) => {
    await expect(page.locator('#pciPeriod')).toBeVisible();
  });

  test('should disable GDPR buttons when no data subject ID entered', async ({ page }) => {
    await expect(page.locator('button:has-text("Export Data")')).toBeDisabled();
    await expect(page.locator('button:has-text("Request Erasure")')).toBeDisabled();
  });

  test('should enable GDPR buttons when data subject ID entered', async ({ page }) => {
    await page.fill('#dataSubjectId', '00000000-0000-0000-0000-000000000001');
    await expect(page.locator('button:has-text("Export Data")')).toBeEnabled();
    await expect(page.locator('button:has-text("Request Erasure")')).toBeEnabled();
  });

  test('should navigate to audit logs', async ({ page }) => {
    await page.click('text=View Audit Logs →');
    await expect(page).toHaveURL(/\/compliance\/audit-logs/);
  });
});

test.describe('Audit Logs', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/compliance/audit-logs');
  });

  test('should display audit logs page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Audit Logs');
  });

  test('should have event type filter', async ({ page }) => {
    const filter = page.locator('#eventTypeFilter');
    await expect(filter).toBeVisible();
    await expect(filter).toContainText('All Events');
  });

  test('should have back button to compliance', async ({ page }) => {
    await page.click('text=← Back to Compliance');
    await expect(page).toHaveURL(/\/compliance$/);
  });
});
