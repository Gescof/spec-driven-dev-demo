# Research: Pet Shop UI Redesign

**Feature**: 004-ui-redesign
**Phase**: 0 — Research
**Date**: 2026-05-12

## Decision 1: Warm Color Palette

**Decision**: Introduce warm-toned CSS custom properties scoped to the homepage. Do NOT replace existing tokens globally — other pages retain the current blue palette to prevent regressions.

**Extracted color tokens from reference image**:

| Token | Value | Use |
|-------|-------|-----|
| `--color-bg-warm` | `#FAF3E8` | Page background for homepage sections |
| `--color-cta` | `#E87040` | Primary CTA buttons (SHOP NOW) |
| `--color-cta-dark` | `#C95A2A` | CTA hover/active state |
| `--color-card-warm` | `#F0E6D3` | Category and product card backgrounds |
| `--color-hero-bg` | `#F5EBD8` | Hero section background (slightly darker cream) |

**Rationale**: Warm earth tones signal approachability — appropriate for a pet care brand. Orange is a high-visibility, action-oriented CTA color. The reference image is unambiguous: cream background, orange button, warm card tiles.

**Alternatives considered**:
- Global token replacement (update `:root`): Rejected — would break catalog, cart, login, and account pages which are out of scope.
- Per-page CSS file: Considered — rejected in favour of scoped classes within `style.css` to preserve single-file architecture.

---

## Decision 2: Typography

**Decision**: Use `Georgia, serif` for the hero headline. Keep `system-ui, sans-serif` for navigation, body text, and product cards.

**Rationale**: The reference image uses a display/serif font for "Treat Your Pet like royalty!" Georgia is available on all major platforms without a network request, adding zero load overhead.

**Alternatives considered**:
- Playfair Display via Google Fonts: Rejected — requires an external CDN request per load. No build tool exists to self-host. Adds a render-blocking resource.
- Keep sans-serif for everything: Rejected — the serif headline is a core visual identity element of the reference design; omitting it would produce a noticeably different result.

---

## Decision 3: Image Assets

**Decision**: Use `background-color` CSS placeholders for hero, category, and customer-favorites images during implementation. Real photographs are sourced separately by the design/content team and dropped into `frontend/images/`.

**Rationale**: The spec Assumptions section explicitly states "placeholder images are acceptable for initial implementation." The `ProductSummary.imageUrl` field in the existing API contract already supports real image URLs — no structural change is needed when real assets arrive.

**File name conventions for future real assets**:

| Section | File | Placeholder color |
|---------|------|-------------------|
| Hero | `images/hero-pet.jpg` | `#D4A96A` (warm golden) |
| Category: Cats | `images/cat.jpg` | `#E8D5B5` |
| Category: Dogs | `images/dog.jpg` | `#DCC9A8` |
| Category: Birds | `images/bird.jpg` | `#D4E8B5` |
| Category: Small Pets | `images/small-pet.jpg` | `#E8CBA8` |
| Customer Favorites (4 slots) | `images/fav-1.jpg` … `fav-4.jpg` | Warm cream variants |

**Alternatives considered**:
- Unsplash CDN image URLs: Rejected — external CDN dependency, fragile in offline/CI environments, image licensing must be verified per image.
- Embedded base64 images: Rejected — inflates HTML significantly; maintenance burden when real assets arrive.

---

## Decision 4: E2E Test Strategy (Constitution III compliance)

**Decision**: Add Playwright as a devDependency under `frontend/package.json`. Write Playwright E2E tests covering all P1 acceptance scenarios **before** implementing HTML/CSS changes.

**Rationale**: Constitution Principle III ("Test-First") is NON-NEGOTIABLE. It requires tests to exist and fail (Red) before implementation begins. Playwright is the minimum viable test runner for a vanilla HTML/CSS/JS app — it verifies visual rendering and DOM structure that jest/jsdom cannot.

**Playwright scope**:
- Verify all five homepage sections render (nav, hero, categories, products, customer favorites)
- Verify orange CTA button color and visibility
- Verify category card click routes to `catalog.html?categoryId={id}`
- Verify WCAG 2.1 AA via `@axe-core/playwright` accessibility audit
- Verify layout at 1280px desktop and 375px mobile viewport

**Alternatives considered**:
- Manual browser checklist: Rejected — not repeatable, cannot satisfy Red phase of TDD, no CI integration.
- Jest + jsdom: Rejected — cannot render CSS or verify visual layout; would not catch the most important class of defects for this feature.
- Cypress: Considered — Playwright preferred for lighter install footprint (~200MB vs ~500MB) and first-class accessibility plugin.

---

## Decision 5: Scope of Navigation Redesign

**Decision**: Apply the new navigation layout ("Pet Store" brand, links: Home, Shop, Dogs, Cats, Birds, Blog + icon buttons for cart/account/search) to `index.html` only.

**Rationale**: The spec Assumption states "existing routing and navigation logic remain unchanged." Extending the new nav to all pages would affect catalog, cart, login, and account pages — out of scope per spec.

Category links (Dogs, Cats, Birds) in the new nav will use query params: `catalog.html?categoryId={id}`, populated dynamically from `GET /api/v1/categories`. The "Shop" link goes to `catalog.html` (no filter). "Home" goes to `index.html`. "Blog" is a placeholder link (no blog page exists; links to `#`).

**Alternatives considered**:
- Global nav update: Rejected — out of scope; requires a separate spec and full regression testing of all pages.
- Server-side nav include: N/A — no templating system; this is a static HTML MPA.
