# Research: DTO Records and YAML Config

**Branch**: `003-dto-records-yaml-config` | **Date**: 2026-05-12

## Java Records in Spring Boot 3 / Java 21

**Decision**: Convert all eligible DTO classes to Java records.

**Rationale**: Java 21 is confirmed (pom.xml `<java.version>21</java.version>`). Records are stable (not preview) since Java 16. Spring Boot 3.2.5 bundles Jackson 2.16, which has first-class record support: it uses the canonical constructor for deserialisation and the accessor methods (e.g., `email()`) for serialisation, producing identical JSON output to the previous getter-based approach.

**Alternatives considered**:
- Keep classes, add Lombok `@Value` / `@Builder`: Adds a compile-time dependency and annotation processing. Records achieve the same with zero dependencies.
- Keep plain classes: No benefit; records are strictly simpler for data-only types.

---

## Jackson Serialisation with Records

**Decision**: No `@JsonProperty` or `@JsonCreator` annotations are needed on converted records.

**Rationale**: Jackson 2.12+ supports records natively. It detects the canonical constructor automatically and maps JSON keys to record component names. The serialised field names are derived from the component names (e.g., `imageUrl`, `grandTotal`) — identical to the previous getter-derived names.

**Special case — `boolean available`**: The old `isAvailable()` JavaBean accessor serialised the JSON key as `"available"`. Records use `available()`, which also serialises as `"available"`. No change in output.

**Alternatives considered**:
- Add `@JsonProperty` on each record component: Verbose, unnecessary with Jackson 2.12+.
- Use `@JsonAutoDetect` to force field detection: Introduces a global configuration change not needed here.

---

## Jakarta Validation on Record Components

**Decision**: Move validation annotations to record component parameters in the compact constructor declaration.

**Rationale**: Spring Boot 3 uses Hibernate Validator 8, which supports validation constraints on record components. Annotations placed on the component parameter (e.g., `@NotBlank String email`) are applied at construction time when `@Valid` is present on the controller parameter. Behaviour is identical to the previous field-annotation approach.

**Alternatives considered**:
- Annotate the canonical constructor parameters: Works but more verbose; compact constructor annotation is the idiomatic Java 21 style.

---

## Inherited DTO Classes — Flattening Decision

**Decision**: Eliminate inheritance by converting `OrderDetailResponse` and `ProductDetailResponse` to flat records containing all fields explicitly. `OrderSummaryResponse` and `ProductSummaryResponse` also become flat records independently.

**Rationale**: Java records are `final` and cannot be extended. The inheritance in the current design (`OrderDetailResponse extends OrderSummaryResponse`) is a code-reuse pattern, not a genuine OOP relationship. Flattening means:
- `OrderSummaryResponse` record: `id, orderNumber, status, placedAt, totalAmount, itemCount`
- `OrderDetailResponse` record: `id, orderNumber, status, placedAt, totalAmount, itemCount, items`
- `ProductSummaryResponse` record: `id, name, price, imageUrl, available, category`
- `ProductDetailResponse` record: `id, name, price, imageUrl, available, category, description`

The duplicated fields are a small, contained redundancy. Each record is self-documenting and requires no superclass lookup to understand its structure. The service layer already constructs each type explicitly, so the constructors in service classes are updated to match the flat record signatures — a mechanical change.

**Alternatives considered**:
- Keep parent classes as regular (non-record) classes and make only the childless classes records: Achieves partial conversion but leaves the inheritance hierarchy unchanged. Rejected because the end state has more types to maintain.
- Extract a common interface: Adds abstraction without a concrete need. YAGNI.
- Use composition (each detail response holds a summary instance): Changes the JSON shape (`{"summary": {...}, "items": [...]}` instead of a flat object). Breaking API contract change — rejected.

---

## Properties-to-YAML Migration

**Decision**: Replace `application.properties` and `application-test.properties` with `application.yml` and `application-test.yml` respectively. Delete the original `.properties` files.

**Rationale**: Spring Boot automatically detects both formats. YAML allows hierarchical grouping of the `spring.datasource.*`, `spring.jpa.*` keys, which is more readable and the community standard for Spring Boot projects. No code changes are required — Spring's `Environment` abstraction is format-agnostic.

**Key mapping rules applied**:
- Dotted keys → YAML hierarchy (e.g., `spring.datasource.url` → `spring.datasource.url` under nested keys)
- Empty password value in `application-test.properties` (`spring.datasource.password=`) → YAML empty string: `password: ""`
- H2 JDBC URL with semicolons (`jdbc:h2:mem:petshop_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`) must be quoted in YAML to avoid the semicolon being interpreted as a comment.
- Comment lines (`# CORS: allow the frontend dev origin`) are retained in YAML as `#` comments.

**Alternatives considered**:
- Keep `.properties`, add `.yml` alongside: Spring Boot loads both; the merge order can cause subtle surprises. Single source of truth is cleaner.
- Use `application.yaml` extension: Spring supports both `.yml` and `.yaml`. The project convention follows `.yml` (shorter, more common in Spring documentation).

---

## File Locations Confirmed

| Old file | New file |
|----------|----------|
| `backend/src/main/resources/application.properties` | `backend/src/main/resources/application.yml` |
| `backend/src/test/resources/application-test.properties` | `backend/src/test/resources/application-test.yml` |

**All 18 DTO classes** in `backend/src/main/java/com/petshop/dto/` are eligible for conversion (14 directly; 4 via flattening the inheritance hierarchy).
