# Tasks: Pet Shop UI Redesign

**Input**: Design documents from `specs/004-ui-redesign/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Organization**: Tasks are grouped by user story (P1 → P2 → P3) to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files or non-overlapping sections, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths are included in every description

---

## Phase 1: Setup

**Purpose**: Test infrastructure and image placeholder assets

- [X] T001 Create `frontend/package.json` with `"devDependencies": {"@playwright/test": "^1.43.0", "@axe-core/playwright": "^4.9.0"}` and `"scripts": {"test": "playwright test"}`
- [X] T002 Create `frontend/playwright.config.js` with base URL `http://localhost`, single `chromium` project, and test directory `./tests`
- [X] T003 [P] Create `frontend/tests/homepage.spec.js` as an empty placeholder file (will be populated per user story)
- [X] T004 [P] Create `frontend/images/` directory and add warm-color SVG placeholder files: `hero-pet.jpg`, `cat.jpg`, `dog.jpg`, `bird.jpg`, `small-pet.jpg`, `fav-1.jpg`, `fav-2.jpg`, `fav-3.jpg`, `fav-4.jpg` (each a minimal valid image file with warm background color)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: CSS token infrastructure shared by all user story phases

**⚠️ CRITICAL**: No user story implementation can begin until this phase is complete

- [X] T005 Add five warm color tokens to `:root` in `frontend/css/style.css`: `--color-bg-warm: #FAF3E8`, `--color-hero-bg: #F5EBD8`, `--color-card-warm: #F0E6D3`, `--color-cta: #E87040`, `--color-cta-dark: #C95A2A`
- [X] T006 [P] Add base homepage section container styles (`.home-section` with padding and `--color-bg-warm` background, `.home-section__title` centered bold uppercase heading) to `frontend/css/style.css`

**Checkpoint**: CSS tokens available — all user story phases can now proceed in parallel

---

## Phase 3: User Story 1 — Browse Pet Shop Homepage (Priority: P1) 🎯 MVP

**Goal**: The homepage displays the new warm-branded layout: updated navigation bar, hero section with headline and CTA, and a Popular Products grid

**Independent Test**: Open `http://localhost`, verify the navigation shows "Pet Store", the hero shows "Treat Your Pet like royalty!" with an orange SHOP NOW button and a FREE DELIVERY badge, and the Popular Products section renders at least 4 product cards with names and prices

### Tests for User Story 1 (Constitution III — Test-First)

> **Write these tests FIRST and confirm they FAIL (Red) before implementing T010–T016**

- [X] T007 [US1] Write Playwright test: navigate to homepage; assert `.nav__brand` text equals "Pet Store"; assert nav contains links for "Home" and "Shop" in `frontend/tests/homepage.spec.js`
- [X] T008 [P] [US1] Write Playwright test: assert hero section visible; assert text "Treat Your Pet like royalty!" present; assert button labelled "SHOP NOW" has orange background (`--color-cta`); assert "FREE DELIVERY" text visible in `frontend/tests/homepage.spec.js`
- [X] T009 [P] [US1] Write Playwright test: assert `#popular-products` section heading "POPULAR PRODUCTS" visible; assert at least one `.product-card--warm` element rendered with non-empty product name and price text in `frontend/tests/homepage.spec.js`
- [X] T010 [P] [US1] Write Playwright accessibility test: use `@axe-core/playwright` to run WCAG 2.1 AA audit on the full homepage; assert zero violations in `frontend/tests/homepage.spec.js`

### Implementation for User Story 1

- [X] T011 [US1] Update navigation bar in `frontend/index.html`: change `.nav__brand` link text to "Pet Store"; add nav links for Home (`href="index.html"`), Shop (`href="catalog.html"`), Dogs (`id="nav-dogs" href="#"`), Cats (`id="nav-cats" href="#"`), Birds (`id="nav-birds" href="#"`), Blog (`href="#"`); replace Cart and Account text links with `.nav__icon-btn` icon buttons (SVG cart icon with `id="nav-cart-btn"` and cart-count badge, SVG account icon with `id="nav-account-btn"`); preserve auth-state `display:none` guard classes from `auth.js`
- [X] T012 [P] [US1] Replace `.hero` section in `frontend/index.html` with new warm hero markup: outer `.hero--warm` section, `.hero__content` column with `.hero__headline` ("Treat Your Pet like royalty!" in `<h1>`), `.btn--cta` anchor to `catalog.html` ("SHOP NOW"), `.hero__badge` span ("FREE DELIVERY"); `.hero__image` column with `<img src="images/hero-pet.jpg" alt="Happy pet">` placeholder
- [X] T013 [P] [US1] Replace existing `#featured-products` section in `frontend/index.html` with `#popular-products` section: heading "POPULAR PRODUCTS", `<div id="popular-products-grid" class="home-product-grid"></div>` container (populated by JS)
- [X] T014 [US1] Add navigation icon button CSS to `frontend/css/style.css`: `.nav__icon-btn` (borderless, cursor pointer, 24×24 flex center); update `.nav__brand` to dark text color (`--color-text`) with `font-weight: 800`
- [X] T015 [P] [US1] Add hero section CSS to `frontend/css/style.css`: `.hero--warm` (two-column flex layout, `background: var(--color-hero-bg)`, min-height 380px); `.hero__headline` (`font-family: Georgia, serif`, `font-size: var(--font-size-3xl)`, `font-weight: 700`); `.hero__badge` (uppercase letter-spacing small text); `.btn--cta` (`background: var(--color-cta)`, white text, rounded, uppercase, padding 0.75rem 1.5rem, hover uses `--color-cta-dark`)
- [X] T016 [P] [US1] Add Popular Products section CSS to `frontend/css/style.css`: `.home-product-grid` (4-column CSS grid, gap `var(--space-4)`); `.product-card--warm` (`background: var(--color-card-warm)`, rounded corners `calc(var(--radius)*2)`, overflow hidden, hover shadow transition)
- [X] T017 [US1] Update `frontend/js/index.js`: change `renderCard` function to use class `product-card--warm` instead of `product-card`; change grid element `getElementById` from `featured-products` to `popular-products-grid`; slice API results to first 4: `products.slice(0, 4)`

**Checkpoint**: User Story 1 independently testable — run Playwright tests (should be Green); verify homepage visually at `http://localhost`

---

## Phase 4: User Story 2 — Navigate to Category (Priority: P2)

**Goal**: The homepage renders a Categories section with 4 animal-type cards that route to the catalog filtered by category when clicked

**Independent Test**: Verify the CATEGORIES section shows at least one image card with an animal name label; click a card and confirm the browser navigates to `catalog.html?categoryId={n}`

### Tests for User Story 2

> **Write this test FIRST and confirm it FAILS (Red) before implementing T019–T021**

- [X] T018 [US2] Write Playwright test: assert `#categories-grid` section with heading "CATEGORIES" visible; assert at least one `.category-card` element rendered with a non-empty label; click first category card; assert URL contains `catalog.html?categoryId=` in `frontend/tests/homepage.spec.js`

### Implementation for User Story 2

- [X] T019 [US2] Add Categories section HTML scaffold to `frontend/index.html`: insert as first section inside `<main>` (before the hero), with heading "CATEGORIES" and `<div id="categories-grid" class="category-grid"></div>` (populated by JS)
- [X] T020 [P] [US2] Add category card CSS to `frontend/css/style.css`: `.category-grid` (4-column CSS grid, gap `var(--space-4)`, padding `var(--space-8) var(--space-6)`); `.category-card` (block link, `background: var(--color-card-warm)`, rounded, overflow hidden, text-decoration none, hover shadow); `.category-card__img` (width 100%, aspect-ratio 1/1, object-fit cover, background `var(--color-card-warm)`); `.category-card__label` (`font-weight: 600`, `font-size: var(--font-size-lg)`, text-align center, padding `var(--space-2)`)
- [X] T021 [US2] Update `frontend/js/index.js`: add `loadCategories()` async function that calls `apiFetch('GET', '/categories')`, maps each category to an `<a class="category-card">` element using image mapping (cat → `images/cat.jpg`, dog → `images/dog.jpg`, bird → `images/bird.jpg`, default → `images/small-pet.jpg`), sets `href="catalog.html?categoryId={id}"`, appends to `#categories-grid`; also sets `href` on `#nav-dogs`, `#nav-cats`, `#nav-birds` nav links using matching category IDs; call `loadCategories()` from `DOMContentLoaded` handler

**Checkpoint**: User Story 2 independently testable — categories grid populates from API; each card routes to correct filtered catalog page

---

## Phase 5: User Story 3 — View Customer Favorites Section (Priority: P3)

**Goal**: The homepage renders a "Customer Favorites" section at the bottom with a warm-toned 4-card pet image grid

**Independent Test**: Scroll to the bottom of the homepage; verify a "Customer Favorites" heading and exactly 4 `.favorites-card` image cards are visible with the warm aesthetic

### Tests for User Story 3

> **Write this test FIRST and confirm it FAILS (Red) before implementing T023–T024**

- [X] T022 [US3] Write Playwright test: scroll to `#customer-favorites` section; assert heading "Customer Favorites" visible; assert exactly 4 `.favorites-card` elements present; assert each card has an `<img>` with a non-empty `alt` attribute in `frontend/tests/homepage.spec.js`

### Implementation for User Story 3

- [X] T023 [US3] Add Customer Favorites section to `frontend/index.html`: append after `#popular-products` section inside `<main>` — `<section id="customer-favorites" class="home-section">`, heading "Customer Favorites", `<div class="favorites-grid">` containing 4 `<div class="favorites-card"><img src="images/fav-{n}.jpg" alt="{descriptive alt text}" loading="lazy"></div>` elements
- [X] T024 [US3] Add Customer Favorites CSS to `frontend/css/style.css`: `.favorites-grid` (4-column CSS grid, gap `var(--space-4)`, padding `var(--space-8) var(--space-6)`); `.favorites-card` (aspect-ratio 1/1, overflow hidden, border-radius `calc(var(--radius)*2)`, background `var(--color-card-warm)`); `.favorites-card img` (width 100%, height 100%, object-fit cover)

**Checkpoint**: User Story 3 independently testable — full homepage shows all five sections; run `npx playwright test` for complete suite

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Responsive layout, image error fallback, regression verification, and final QA

- [X] T025 [P] Add mobile responsive CSS (`@media (max-width: 640px)`) for all new homepage sections in `frontend/css/style.css`: `.category-grid`, `.home-product-grid`, `.favorites-grid` collapse to 2-column; `.hero--warm` stacks to single column with image below content; `.nav__links` wraps or hides low-priority links
- [X] T026 [P] Add image error fallback CSS in `frontend/css/style.css`: `img.category-card__img:not([src])`, `.category-card--no-img`, `.product-card--no-img` display `--color-card-warm` background with a centered "No image" label
- [X] T027 Run full Playwright test suite from `frontend/`: `npx playwright install chromium && npx playwright test`; confirm all tests pass at 1280px desktop viewport
- [X] T028 Run Playwright tests at 375px mobile viewport: update `playwright.config.js` to add mobile project; run `npx playwright test`; fix any layout overflow or broken elements found
- [X] T029 [P] Open `catalog.html`, `cart.html`, `login.html`, and `account.html` in browser; confirm no visual regressions (existing blue palette intact, layouts unbroken, no missing styles)
- [X] T030 Resolve any WCAG 2.1 AA violations found by the axe-core Playwright test (T010): fix color contrast issues, missing `alt` attributes, or missing focus indicators
- [X] T031 [P] Update `specs/004-ui-redesign/quickstart.md` if any commands, paths, or steps changed during implementation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 — independent of US2, US3
- **US2 (Phase 4)**: Depends on Phase 2 — independent of US1, US3
- **US3 (Phase 5)**: Depends on Phase 2 — independent of US1, US2
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **US1 (P1)**: Blocked only by Foundational. Can proceed in parallel with US2 and US3 after Phase 2.
- **US2 (P2)**: Blocked only by Foundational. Independent of US1 and US3.
- **US3 (P3)**: Blocked only by Foundational. Independent of US1 and US2.

### Within Each User Story

- Tests (T007–T010, T018, T022) MUST be written and confirmed failing before any implementation in the same story
- HTML scaffold tasks (T012, T013, T019, T023) can run in parallel with CSS tasks (T015, T016, T020, T024) since they modify different files
- JS rendering tasks (T017, T021) depend on their respective HTML scaffolds being in place
- CSS tokens (T005) must be present before component CSS rules reference them

---

## Parallel Example: User Story 1

```
# Step 1 — Write tests together (all append to homepage.spec.js):
T008: Hero section Playwright test
T009: Popular Products Playwright test
T010: Accessibility Playwright test

# Step 2 — Confirm tests FAIL (Red), then implement in parallel:
T012: Hero section HTML  (index.html)
T013: Popular Products HTML scaffold  (index.html — non-overlapping section)
T015: Hero CSS  (style.css — .hero--warm rules)
T016: Popular Products CSS  (style.css — .home-product-grid rules)

# Step 3 — Sequential after HTML scaffold:
T017: Update index.js rendering (depends on T013 grid ID)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (T001–T004) — test infrastructure + placeholders
2. Complete Phase 2 (T005–T006) — CSS tokens
3. Write US1 tests (T007–T010) — confirm they FAIL
4. Implement US1 (T011–T017) — confirm tests PASS
5. **STOP and VALIDATE**: `http://localhost` shows working nav + hero + products
6. Demo and review; proceed to US2 only when US1 is approved

### Incremental Delivery

1. Phase 1 + 2 → infrastructure ready
2. US1 → working nav + hero + popular products (MVP homepage)
3. US2 → categories grid with routing (browse-by-animal-type)
4. US3 → customer favorites section (social proof)
5. Phase 6 Polish → responsive, accessible, regression-checked

### Parallel Team Strategy

With multiple developers, after Phase 2 completes:

- Developer A: US1 (T007–T017) — nav, hero, popular products
- Developer B: US2 (T018–T021) — categories grid + JS fetch
- Developer C: US3 (T022–T024) — customer favorites section

All write their tests first independently, implement, then converge on Phase 6 together.

---

## Notes

- [P] tasks target different files or non-overlapping CSS rule sets — safe to run simultaneously
- [US?] labels map each task to a user story for traceability and MVP scoping
- Warm CSS tokens (T005) are additive — existing pages are unaffected
- Placeholder images in `frontend/images/` are drop-in replaceable with real photography; no HTML/CSS structural changes needed
- Commit after each user story checkpoint to preserve independent, rollback-safe increments
- If the Playwright axe-core audit (T010/T030) flags orange-on-cream contrast, darken `--color-cta` to `#C95A2A` or switch to white text — check and adjust early
