// Playwright E2E tests for the Pet Shop homepage redesign.
// Tests are written before implementation (TDD Red phase).
// Run with: npx playwright test

const { test, expect } = require('@playwright/test');
const { AxeBuilder } = require('@axe-core/playwright');

// ─── User Story 1: Browse Pet Shop Homepage ──────────────────────────────────

test('US1 — nav brand and links', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.nav__brand')).toHaveText('Pet Store');
    await expect(page.getByRole('link', { name: 'Home', exact: true })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Shop', exact: true })).toBeVisible();
});

test('US1 — hero section content', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.hero--warm')).toBeVisible();
    await expect(page.locator('.hero__headline')).toContainText('Treat Your Pet like royalty!');
    const shopNow = page.getByRole('link', { name: 'SHOP NOW', exact: true });
    await expect(shopNow).toBeVisible();
    const bgColor = await shopNow.evaluate(el => getComputedStyle(el).backgroundColor);
    expect(bgColor).toMatch(/rgb\(/);
    await expect(page.locator('.hero__badge')).toContainText('FREE DELIVERY');
});

test('US1 — popular products section', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('#popular-products')).toBeVisible();
    await expect(page.locator('#popular-products .home-section__title')).toContainText('POPULAR PRODUCTS');
    const cards = page.locator('.product-card--warm');
    await expect(cards.first()).toBeVisible({ timeout: 10000 });
    const firstName = await cards.first().locator('.product-card__name').textContent();
    expect(firstName?.trim().length).toBeGreaterThan(0);
    const firstPrice = await cards.first().locator('.product-card__price').textContent();
    expect(firstPrice?.trim().length).toBeGreaterThan(0);
});

test('US1 — WCAG 2.1 AA accessibility audit', async ({ page }) => {
    await page.goto('/');
    const results = await new AxeBuilder({ page })
        .withTags(['wcag2a', 'wcag2aa'])
        .analyze();
    expect(results.violations).toEqual([]);
});

// ─── User Story 2: Navigate to Category ─────────────────────────────────────

test('US2 — categories section and routing', async ({ page }) => {
    await page.goto('/');
    const section = page.locator('#categories-section');
    await expect(section).toBeVisible();
    await expect(section.locator('.home-section__title')).toContainText('CATEGORIES');
    const cards = page.locator('.category-card');
    await expect(cards.first()).toBeVisible({ timeout: 10000 });
    const label = await cards.first().locator('.category-card__label').textContent();
    expect(label?.trim().length).toBeGreaterThan(0);
    await cards.first().click();
    await expect(page).toHaveURL(/catalog\.html\?categoryId=/);
});

// ─── User Story 3: View Customer Favorites Section ───────────────────────────

test('US3 — customer favorites section', async ({ page }) => {
    await page.goto('/');
    const section = page.locator('#customer-favorites');
    await section.scrollIntoViewIfNeeded();
    await expect(section).toBeVisible();
    await expect(section).toContainText('Customer Favorites');
    const favCards = page.locator('.favorites-card');
    await expect(favCards).toHaveCount(4);
    for (let i = 0; i < 4; i++) {
        const img = favCards.nth(i).locator('img');
        await expect(img).toBeVisible();
        const alt = await img.getAttribute('alt');
        expect(alt?.trim().length).toBeGreaterThan(0);
    }
});
