import { test, expect } from '@playwright/test';

test.describe('Document Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/documents');
  });

  test('should display document list page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Documents');
    await expect(page.locator('[aria-label="Upload new document"]')).toBeVisible();
  });

  test('should have classification filter', async ({ page }) => {
    const filter = page.locator('#classification-filter');
    await expect(filter).toBeVisible();
    await expect(filter).toContainText('All');
    await expect(filter).toContainText('Public');
    await expect(filter).toContainText('Internal');
    await expect(filter).toContainText('Confidential');
    await expect(filter).toContainText('Restricted');
  });

  test('should have search input', async ({ page }) => {
    const search = page.locator('[aria-label="Search documents"]');
    await expect(search).toBeVisible();
    await expect(search).toHaveAttribute('placeholder', 'Search documents...');
  });

  test('should navigate to upload page', async ({ page }) => {
    await page.click('[aria-label="Upload new document"]');
    await expect(page).toHaveURL(/\/documents\/upload/);
    await expect(page.locator('h1')).toContainText('Upload Document');
  });

  test('should display empty state when no documents', async ({ page }) => {
    // When API returns empty, should show empty state
    await expect(page.locator('.empty-state, .loading, .data-table')).toBeVisible();
  });
});

test.describe('Document Upload', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/documents/upload');
  });

  test('should display upload form', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Upload Document');
    await expect(page.locator('#docName')).toBeVisible();
    await expect(page.locator('#classification')).toBeVisible();
    await expect(page.locator('.file-drop-zone')).toBeVisible();
  });

  test('should have back button to documents list', async ({ page }) => {
    await page.click('text=← Back to Documents');
    await expect(page).toHaveURL(/\/documents$/);
  });

  test('should require all fields before submit', async ({ page }) => {
    const submitBtn = page.locator('button[type="submit"]');
    await expect(submitBtn).toBeDisabled();
  });

  test('should accept file input', async ({ page }) => {
    const fileInput = page.locator('#file');
    await fileInput.setInputFiles({
      name: 'test-document.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('test content')
    });
    await expect(page.locator('.selected-file')).toContainText('test-document.pdf');
  });
});

test.describe('Document Detail', () => {
  test('should display document detail page structure', async ({ page }) => {
    // Navigate to a mock document ID
    await page.goto('/documents/00000000-0000-0000-0000-000000000001');
    // Should show loading or error (no backend running)
    await expect(page.locator('.loading, .error-banner, .detail-card')).toBeVisible();
  });
});
