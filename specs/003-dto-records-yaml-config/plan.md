# Implementation Plan: DTO Records and YAML Config

**Branch**: `003-dto-records-yaml-config` | **Date**: 2026-05-12 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/003-dto-records-yaml-config/spec.md`

## Summary

Convert all 18 backend DTO classes to Java records, eliminating inheritance hierarchies via flattening where necessary, and migrate both Spring Boot properties files (`application.properties`, `application-test.properties`) to YAML format. No API contract changes are introduced; the JSON output and validation behaviour are identical before and after.

## Technical Context

**Language/Version**: Java 21 (records are stable, no preview flags)
**Primary Dependencies**: Spring Boot 3.2.5, Jackson 2.16 (bundled), Hibernate Validator 8, Maven 3.9+
**Storage**: PostgreSQL 15 (production), H2 in-memory (test profile)
**Testing**: JUnit 5 + MockMvc + Spring Security Test (via `spring-boot-starter-test`)
**Target Platform**: JVM / Linux Docker container
**Project Type**: web-service (REST API backend — part of a full-stack pet shop app)
**Performance Goals**: N/A — this is a structural refactoring with no performance targets
**Constraints**: JSON output must be byte-for-byte identical to pre-refactoring output; all existing tests must pass without modification to test logic
**Scale/Scope**: 18 DTO classes, 2 configuration files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Spec-First Development | ✅ Pass | `specs/003-dto-records-yaml-config/spec.md` exists and is complete |
| II. Contract-Driven Design | ✅ Pass | No API contract changes; existing `specs/001-pet-shop-webapp/contracts/openapi.yaml` remains valid and authoritative |
| III. Test-First (NON-NEGOTIABLE) | ✅ Pass | This is a pure refactoring with no new functionality. The existing test suite serves as the regression guard. No new behaviour is introduced, so there is nothing to write a failing test for. The test suite must pass after every conversion step. |
| IV. End-to-End Testability | ✅ Pass | Existing integration and controller tests cover the converted DTOs end-to-end |
| V. Simplicity & Incremental Delivery | ✅ Pass | Records reduce code; flattening eliminates the inheritance hierarchy without adding abstraction |

**Post-Phase 1 re-check**: All principles remain satisfied. No API contract changes, no new dependencies, no new abstractions.

## Project Structure

### Documentation (this feature)

```text
specs/003-dto-records-yaml-config/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/petshop/
│   │   │   ├── dto/                  ← 18 DTO classes → records
│   │   │   │   ├── AddToCartRequest.java
│   │   │   │   ├── CartItemResponse.java
│   │   │   │   ├── CartResponse.java
│   │   │   │   ├── CategoryResponse.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── OrderDetailResponse.java
│   │   │   │   ├── OrderLineItemResponse.java
│   │   │   │   ├── OrderSummaryResponse.java
│   │   │   │   ├── PlaceOrderRequest.java
│   │   │   │   ├── ProductDetailResponse.java
│   │   │   │   ├── ProductPageResponse.java
│   │   │   │   ├── ProductSummaryResponse.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── UpdateCartItemRequest.java
│   │   │   │   ├── UpdateProfileRequest.java
│   │   │   │   ├── UserProfileResponse.java
│   │   │   │   └── ValidationErrorResponse.java
│   │   │   ├── service/              ← Constructor call sites updated (mechanical)
│   │   │   │   ├── OrderService.java
│   │   │   │   └── ProductService.java
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java  ← ErrorResponse / ValidationErrorResponse sites
│   │   └── resources/
│   │       ├── application.yml       ← replaces application.properties
│   │       └── application.properties  (DELETED)
│   └── test/
│       └── resources/
│           ├── application-test.yml  ← replaces application-test.properties
│           └── application-test.properties  (DELETED)
```

**Structure Decision**: Web application (backend only for this feature). Frontend is untouched. Only the backend `dto/` package and `resources/` directories are modified.

## Implementation Order

### P1: DTO Record Conversion

Convert in this order to keep the build green at each step:

1. **Leaf DTOs with no relationships** (simplest records, no other DTO dependencies):
   - `LoginRequest`, `RegisterRequest`, `PlaceOrderRequest`, `UpdateCartItemRequest`, `UpdateProfileRequest`, `AddToCartRequest`

2. **Response DTOs that are used by other DTOs** (convert before their consumers):
   - `CategoryResponse` (used by `ProductSummaryResponse`)
   - `OrderLineItemResponse` (used by `OrderDetailResponse`)

3. **Inheritance-hierarchy records** (flatten in one commit):
   - `OrderSummaryResponse` and `OrderDetailResponse` together (both become flat records; update `OrderService` constructor calls in the same commit)
   - `ProductSummaryResponse` and `ProductDetailResponse` together (same approach; update `ProductService` constructor calls)

4. **Remaining response DTOs**:
   - `CartItemResponse`, `CartResponse`, `ErrorResponse`, `ValidationErrorResponse`, `ProductPageResponse`, `UserProfileResponse`
   - Update `GlobalExceptionHandler` constructor sites for `ErrorResponse` and `ValidationErrorResponse`

### P2: YAML Configuration Migration

5. Replace `application.properties` with `application.yml`; delete the old file.
6. Replace `application-test.properties` with `application-test.yml`; delete the old file.

## YAML Migration Reference

### application.yml (main)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/petshop
    username: petshop_user
    password: petshop_pass
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

server:
  port: 8080

# CORS: allow the frontend dev origin
petshop:
  cors:
    allowed-origins: http://localhost:5500,http://127.0.0.1:5500
```

### application-test.yml (test)

```yaml
spring:
  datasource:
    # Semicolons in the H2 URL require quoting
    url: "jdbc:h2:mem:petshop_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  security:
    user:
      name: test
      password: test

petshop:
  cors:
    allowed-origins: http://localhost:5500
```

## Complexity Tracking

> No constitution violations. No entries required.
