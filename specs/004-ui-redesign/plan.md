# Implementation Plan: Pet Shop UI Redesign

**Branch**: `004-ui-redesign` | **Date**: 2026-05-12 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/004-ui-redesign/spec.md`

## Summary

Redesign the Pet Shop homepage (`frontend/index.html`) to match a warm cream-and-orange visual identity inspired by the reference design. Changes are confined to the frontend layer: updated CSS custom properties in `style.css`, a restructured `index.html` with five new sections (navigation, hero, categories, popular products, customer favorites), and minor updates to `index.js` to populate the categories section from the existing `GET /api/v1/categories` API. No backend changes. No new API contracts. Playwright E2E tests are introduced as the test-first infrastructure before implementation begins.

## Technical Context

**Language/Version**: HTML5, CSS3, ES2022 (Vanilla JS — no framework, no build tool)
**Primary Dependencies**: None for production; Playwright v1.x as devDependency under `frontend/` for E2E tests
**Storage**: N/A — frontend-only visual redesign
**Testing**: Playwright (E2E + accessibility via axe-core); satisfies Constitution III Test-First requirement
**Target Platform**: Modern web browsers (Chrome, Firefox, Safari, Edge); desktop-first (1280px), responsive down to 375px mobile
**Project Type**: Web application (frontend redesign)
**Performance Goals**: Homepage fully rendered in under 1 second on desktop; all CSS renders without layout shift
**Constraints**: No build tools in production; CSS changes must not regress catalog/cart/login/account pages; images may use placeholders pending real photography; nav redesign scoped to `index.html` only
**Scale/Scope**: 1 HTML page (index.html) + additive CSS to style.css + minor index.js update + Playwright test suite

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Spec-First | ✅ PASS | `specs/004-ui-redesign/spec.md` approved |
| II. Contract-Driven | ✅ N/A | No new or changed API contracts; existing `GET /categories` and `GET /products/featured` used as-is; contract reference: `specs/001-pet-shop-webapp/contracts/openapi.yaml` |
| III. Test-First | ⚠️ JUSTIFIED VIOLATION | No frontend test framework exists. Playwright is introduced before implementation begins. See Complexity Tracking. |
| IV. E2E Testability & Accessibility | ✅ COMPLIANT | Playwright covers all P1 acceptance scenarios; axe-core plugin enforces WCAG 2.1 AA |
| V. Simplicity | ✅ PASS | Changes confined to `index.html` + `style.css` + `index.js`; no new abstractions; YAGNI applied |

**Post-Phase 1 re-check**: All gates remain green. No new violations introduced by design artifacts.

## Project Structure

### Documentation (this feature)

```text
specs/004-ui-redesign/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code

```text
frontend/
├── css/
│   └── style.css          # Additive: new warm color tokens + new homepage section classes
├── js/
│   └── index.js           # Updated: categories section rendered from API
├── images/                # New directory: placeholder images for all redesign sections
│   ├── hero-pet.jpg
│   ├── cat.jpg
│   ├── dog.jpg
│   ├── bird.jpg
│   ├── small-pet.jpg
│   ├── fav-1.jpg
│   ├── fav-2.jpg
│   ├── fav-3.jpg
│   └── fav-4.jpg
├── index.html             # Redesigned: nav, hero, categories, products, favorites sections
├── package.json           # New: Playwright devDependency only (no build tooling)
└── tests/
    └── homepage.spec.js   # Playwright E2E tests (written before implementation)
```

**Structure Decision**: Single web application (frontend + backend). Only the frontend `index.html`, `style.css`, and `index.js` files change. The backend and all other frontend pages are untouched. A new `tests/` directory and `package.json` are added under `frontend/` for Playwright, kept separate from the static production files served by Nginx.

## Design Decisions

### Color Tokens Added to `style.css`

New properties added to `:root` — they coexist with existing tokens and do not override them:

```css
--color-bg-warm:    #FAF3E8;   /* Homepage warm background */
--color-hero-bg:    #F5EBD8;   /* Hero section, slightly deeper cream */
--color-card-warm:  #F0E6D3;   /* Category and product card backgrounds */
--color-cta:        #E87040;   /* Orange primary CTA (SHOP NOW button) */
--color-cta-dark:   #C95A2A;   /* CTA hover/active */
```

### Homepage Sections (in render order)

1. **NavigationBar** — "Pet Store" brand; static links (Home → `index.html`, Shop → `catalog.html`, Blog → `#`); dynamic category links (Dogs, Cats, Birds) resolved from `GET /api/v1/categories`; icon buttons for cart, account, search
2. **HeroSection** — warm cream background; serif "Treat Your Pet like royalty!" headline; orange "SHOP NOW" CTA; "FREE DELIVERY" badge; hero pet image (placeholder → real photo)
3. **CategoriesSection** — 4-column grid of image cards sourced from `GET /api/v1/categories`; each card links to `catalog.html?categoryId={id}`
4. **PopularProductsSection** — 4-column product card grid sourced from first 4 results of `GET /api/v1/products/featured`; each card links to `product.html?id={id}`
5. **CustomerFavoritesSection** — 4-column static image grid with pet photography (placeholders until real assets provided)

### Typography

- Hero headline: `Georgia, serif` (display weight, large)
- All other text: `system-ui, sans-serif` (unchanged)

### Image Strategy

CSS `background-color` placeholders using warm palette colors during development. Drop-in replacement with real JPG assets requires only replacing the `src` attributes and `images/` file contents — no structural HTML/CSS changes needed.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Adding Playwright devDependency (no existing frontend test framework) | Constitution III mandates test-first for all P1 stories; tests must fail (Red) before implementation begins | Manual browser checklist is not repeatable and cannot satisfy the Red phase requirement; jest/jsdom cannot render CSS or verify visual layout — the primary concern of this feature |
