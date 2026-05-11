# Research: Pet Shop Web App

**Branch**: `001-pet-shop-webapp` | **Date**: 2026-05-11 | **Plan**: [plan.md](plan.md)

## R-001: Authentication Mechanism

**Question**: Session-based (HttpSession cookie) or JWT-based authentication for vanilla JS + Spring Boot?

**Decision**: Session-based authentication with Spring Security's HttpSession and a JSESSIONID HttpOnly cookie.

**Rationale**: HttpOnly cookies prevent XSS token theft with zero extra code. Spring Security manages session lifecycle automatically. Vanilla JS uses `fetch()` with `credentials: 'include'` — no Authorization header management needed. Works out of the box with Spring Security's `UserDetailsService`.

**Alternatives considered**:
- JWT in localStorage: rejected — localStorage is XSS-accessible; requires frontend token refresh logic.
- JWT in HttpOnly cookie: stateless session management adds `jjwt` dependency with no meaningful advantage at this scale.

---

## R-002: Frontend Serving Strategy

**Question**: Serve frontend from Spring Boot `static/` or from a separate origin with CORS?

**Decision**: Frontend files in a top-level `frontend/` directory, served independently (file:// or any static server). Spring Boot enables CORS for `http://localhost:5500` and `http://127.0.0.1:5500` (Live Server defaults) in development. For a single-JAR build, the frontend can be copied into `src/main/resources/static/`.

**Rationale**: Separate directories enforce the contract-driven design principle — frontend has no access to backend source and must go through the API contract. Simpler frontend iteration without a Java rebuild cycle.

**Alternatives considered**:
- Serve from Spring Boot static/: couples FE/BE deployment; loses independent FE testability.
- Separate Node.js server: adds Node.js as a dependency — rejected as "not minimal."

---

## R-003: Spring Boot Dependency Selection

**Question**: What is the minimal set of Spring Boot starters for all feature requirements?

**Decision**:

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-web` | REST API (embedded Tomcat) |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM for PostgreSQL |
| `spring-boot-starter-security` | Session-based auth, BCrypt password encoding |
| `spring-boot-starter-validation` | Bean Validation (`@Valid`, `@NotBlank`, etc.) |
| `postgresql` (runtime) | PostgreSQL JDBC driver |
| `spring-boot-starter-test` | JUnit 5, Mockito, Spring MockMvc |

**Rationale**: Six dependencies cover all requirements. No ORM extras (Hibernate is bundled in data-jpa). No mapper library (manual DTO mapping is trivial for 6 entities). No migration tool for v1 (DDL auto sufficient for local demo).

**Alternatives considered**:
- MapStruct: rejected — adds annotation processor complexity; manual mapping is <50 lines for this entity count.
- Flyway/Liquibase: deferred to post-v1 (local dev, `ddl-auto=create-drop` is acceptable for demo scale).
- Spring Boot Actuator: out of scope (no ops monitoring required by spec).

---

## R-004: Java Version

**Decision**: Java 21 (LTS).

**Rationale**: Spring Boot 3.x requires Java 17+. Java 21 is the current LTS with virtual threads (Project Loom) for improved JDBC I/O concurrency. Widely available on all platforms.

**Alternatives considered**: Java 17 (misses virtual threads); Java 22+ (non-LTS, not appropriate for a stable project baseline).

---

## R-005: Cart Persistence Strategy

**Question**: Shopping cart in HTTP session or in PostgreSQL?

**Decision**: Database-backed cart (CartItem records linked to User via Cart entity).

**Rationale**: The spec edge case states "Cart contents are preserved on login; user is prompted to log back in" after session expiry. A session-backed cart is lost on session expiry, violating this requirement. A DB-backed cart survives session restart.

**Alternatives considered**: Session cart (rejected — violates spec edge case); Redis cart (adds Redis dependency, overkill for demo scale).

---

## R-006: Vanilla JS Frontend Architecture

**Decision**: Multi-page application (MPA) — one HTML file per view. JavaScript organized as small page-scoped scripts: `api.js` (shared fetch wrapper), `auth.js` (session state checks), and per-page files (`catalog.js`, `product.js`, `cart.js`, `account.js`, `checkout.js`).

**Rationale**: Simplest approach for vanilla JS — no build step, no bundler, no framework. Each page loads only the JS it needs. Browser native navigation handles transitions. MPA has better default accessibility than JS-driven SPA.

**Alternatives considered**: SPA with History API routing (rejected — custom routing and state management adds unnecessary complexity); Fetch + innerHTML SPA (harder to meet WCAG 2.1 AA with dynamic content injection).

---

## R-007: API Versioning Strategy

**Decision**: URL prefix versioning — all REST endpoints under `/api/v1/`.

**Rationale**: Constitution Principle II requires "versioned API contracts." URL prefix versioning is the simplest strategy, trivial to implement in Spring Boot via `@RequestMapping`, and easy to test from a browser or vanilla JS `fetch()`. Breaking changes increment to `/api/v2/` with a documented migration path in `plan.md`.

**Alternatives considered**: Header versioning (`Accept: application/vnd.petshop.v1+json`) — harder to test from browser; no benefit at this scale.
