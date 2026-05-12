# Quickstart: Pet Shop UI Redesign

**Feature**: 004-ui-redesign

## Prerequisites

- Docker and Docker Compose installed (for the full stack)
- Node.js 18+ installed (Playwright E2E tests only — no build tools used for production)

## Running the Full Stack

```bash
docker compose up --build
```

Homepage: [http://localhost](http://localhost)

The `frontend/` directory is served via Nginx on port 80. Changes to HTML/CSS/JS files are reflected after a rebuild (`docker compose up --build`) or by mounting the directory as a volume for hot-reload in development.

## TDD Workflow (Test-First)

### 1. Install Playwright (first time only)

```bash
cd frontend
npm install
npx playwright install chromium
```

### 2. Run tests before implementing (Red phase)

```bash
cd frontend
npx playwright test
```

All tests should **fail** at this point — that is expected and confirms the tests are meaningful.

### 3. Implement the redesign

Edit `frontend/index.html`, `frontend/css/style.css`, and `frontend/js/index.js` per the plan.

### 4. Run tests again (Green phase)

```bash
cd frontend
npx playwright test
```

All tests should now pass.

## Verifying the Redesign Manually

1. Open [http://localhost](http://localhost)
2. Navigation: "Pet Store" brand name on the left; Home, Shop, Dogs, Cats, Birds, Blog links on the right plus cart/account/search icons
3. Hero: warm cream background, "Treat Your Pet like royalty!" headline in serif font, orange "SHOP NOW" button, "FREE DELIVERY" badge
4. Categories: 4 animal category cards (Cats, Dogs, Birds, Small Pets) with labels
5. Popular Products: at least 4 product cards with name and price
6. Customer Favorites: 4 pet image cards at the bottom of the page
7. Click a category card → confirm routing to `catalog.html?categoryId=X`
8. Resize to 375px width → confirm no horizontal overflow or broken layout

## Accessibility Check

Playwright tests include an axe-core accessibility audit. To run it standalone:

```bash
cd frontend
npx playwright test --grep "accessibility"
```

Key WCAG 2.1 AA checks:
- Color contrast ratio ≥ 4.5:1 for all text (especially orange button text on cream background)
- All images have non-empty `alt` attributes
- Interactive elements are keyboard-reachable (Tab navigation)
- Nav links have visible focus indicators

## File Locations

| File | Purpose |
|------|---------|
| `frontend/index.html` | Homepage — restructured with new sections |
| `frontend/css/style.css` | Global styles — new warm color tokens + new section classes appended |
| `frontend/js/index.js` | Homepage JS — updated to render categories section |
| `frontend/images/` | New directory for placeholder and real pet images |
| `frontend/tests/homepage.spec.js` | Playwright E2E tests for the redesigned homepage |
| `frontend/package.json` | Playwright devDependency (test-only; no build tooling) |
