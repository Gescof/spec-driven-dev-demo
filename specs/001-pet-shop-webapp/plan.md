# Implementation Plan: Pet Shop Web App

**Branch**: `001-pet-shop-webapp` | **Date**: 2026-05-11 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/001-pet-shop-webapp/spec.md`

## Summary

A full-stack pet shop e-commerce web application with a Spring Boot REST API backend and a vanilla HTML/CSS/JavaScript frontend, backed by a local PostgreSQL database. The application covers three priority user journeys: product catalog browsing (P1), user registration and account management (P2), and cart-based shopping and order placement (P3). No real payment processing, image uploads, or admin interface are in scope for v1.

## Technical Context

**Language/Version**: Java 21 (LTS) — backend; JavaScript ES2022 (vanilla) — frontend
**Primary Dependencies**: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, postgresql (runtime), spring-boot-starter-test
**Storage**: PostgreSQL 15+ (local, port 5432); schema managed via `ddl-auto=create-drop` for development
**Testing**: JUnit 5, Spring Boot Test, Mockito (backend); browser-based manual testing (frontend)
**Target Platform**: Local development server; desktop and mobile web browsers (responsive layout required)
**Project Type**: Full-stack web application — Spring Boot REST API + vanilla static frontend (MPA)
**Performance Goals**: Product search results displayed in < 2 seconds (SC-004); all pages fully interactive in < 3 seconds (SC-007)
**Constraints**: No image uploads (external URLs only); simulated payment only (no gateway); single currency USD; English only; no admin/seller roles for v1
**Scale/Scope**: ~8 HTML pages; 6 core entities; 16 REST endpoints; single-developer demo scale

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Spec-First Development | ✅ PASS | `specs/001-pet-shop-webapp/spec.md` exists and complete |
| II. Contract-Driven Design | ✅ PASS | REST contracts defined in `contracts/openapi.yaml` (Phase 1). FE uses `fetch()` against versioned `/api/v1` endpoints only. Breaking changes must increment to `/api/v2/` with migration path. |
| III. Test-First (NON-NEGOTIABLE) | ✅ PASS (enforced) | JUnit 5 + Spring MockMvc for all backend endpoints. Tests MUST be written before implementation (Red-Green-Refactor). Contract tests via `@WebMvcTest`. Integration tests via `@SpringBootTest`. |
| IV. E2E Testability & Accessibility | ✅ PASS (enforced) | Each P1/P2/P3 story is independently testable per spec. WCAG 2.1 AA audit required before each story is closed. |
| V. Simplicity & Incremental Delivery | ✅ PASS | Vanilla JS + minimal Spring Boot starters = YAGNI-compliant. Complexity Tracking table has no entries. Stories delivered P1 → P2 → P3. |

**Post-Phase 1 re-check**: All principles still pass after design. The 6-entity data model maps 1:1 to spec requirements. No Repository pattern, caching, or additional abstraction layers introduced beyond JPA repositories.

## Project Structure

### Documentation (this feature)

```text
specs/001-pet-shop-webapp/
├── plan.md              # This file (/speckit-plan output)
├── research.md          # Phase 0 output (/speckit-plan output)
├── data-model.md        # Phase 1 output (/speckit-plan output)
├── quickstart.md        # Phase 1 output (/speckit-plan output)
├── contracts/
│   └── openapi.yaml     # Phase 1 output (/speckit-plan output) — OpenAPI 3.0
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/petshop/
│   │   │   ├── config/           # SecurityConfig, CorsConfig, WebMvcConfig
│   │   │   ├── controller/       # AuthController, CategoryController,
│   │   │   │                     # ProductController, CartController,
│   │   │   │                     # OrderController, UserController
│   │   │   ├── dto/              # Request and response DTOs (manual mapping)
│   │   │   ├── model/            # JPA entities: Category, Product, User,
│   │   │   │                     # Cart, CartItem, Order, OrderItem
│   │   │   ├── repository/       # Spring Data JPA repositories
│   │   │   ├── service/          # Business logic services
│   │   │   └── PetShopApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/
│   │           └── seed.sql
│   └── test/
│       └── java/com/petshop/
│           ├── controller/       # @WebMvcTest controller + contract tests
│           ├── service/          # Unit tests (Mockito)
│           └── integration/      # @SpringBootTest integration tests
└── pom.xml

frontend/
├── index.html            # Home page (featured products, navigation)
├── catalog.html          # Product catalog (filter by category, keyword search)
├── product.html          # Product detail page
├── login.html            # Login form
├── register.html         # Registration form
├── account.html          # User profile + order history
├── cart.html             # Shopping cart (quantities, remove, totals)
├── checkout.html         # Simulated checkout + confirmation
├── css/
│   └── style.css         # Single stylesheet (responsive, WCAG 2.1 AA)
└── js/
    ├── api.js            # Shared fetch() wrapper (base URL, credentials: include)
    ├── auth.js           # Session state helpers (redirect guards)
    ├── catalog.js
    ├── product.js
    ├── account.js
    ├── cart.js
    └── checkout.js
```

**Structure Decision**: Web application (Option 2 variant). Backend and frontend are in separate top-level directories to enforce contract-driven decoupling. The frontend communicates exclusively via the `/api/v1` REST contract. No Thymeleaf or server-side rendering — all HTML is static, all data fetched via `fetch()` with `credentials: 'include'`.

## Complexity Tracking

> No constitution violations. Table left intentionally empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| — | — | — |

## Performance Notes

> Added during Phase 6 polish (T072). No benchmarks were run; notes reflect design-level analysis with seed data at demo scale (~8 products, 4 categories).

**SC-004 — Product search < 2 seconds**: The `findByFilters` JPQL query uses `LIKE` on `name` and `description`. At seed-data scale this is instant. At catalog scale (thousands of products), full-text search or a trigram index on those columns would be needed. No bottleneck observed with seed data.

**SC-007 — All pages interactive < 3 seconds**: All HTML pages are static files served by Live Server with no build step. JavaScript bundles are minimal vanilla JS. Only risk is cold-start latency on the first Spring Boot request after startup (~1–2 s JVM warm-up). Subsequent requests are fast. No frontend bottleneck observed.
