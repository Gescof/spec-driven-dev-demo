# Feature Specification: DTO Records and YAML Config

**Feature Branch**: `003-dto-records-yaml-config`
**Created**: 2026-05-12
**Status**: Draft
**Input**: User description: "Transform BE DTO classes into records when possible. Also, transform properties file into YAML format"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Backend DTO Classes Converted to Records (Priority: P1)

A backend developer opens the `dto/` package and finds that all eligible data transfer classes are Java records — immutable, concise, and without boilerplate getters, constructors, or `equals`/`hashCode` implementations. Classes that inherit from other DTO classes remain as regular classes until a design decision is made about flattening.

**Why this priority**: Java records reduce boilerplate code and enforce immutability for data-only objects. Converting DTOs that have no inheritance is safe and immediate. This is the core deliverable.

**Independent Test**: Run the full backend test suite after conversion. If all tests pass and the application starts correctly, the conversion is successful. The result is verifiable without any frontend interaction.

**Acceptance Scenarios**:

1. **Given** a DTO class with no superclass (other than Object) and no subclasses, **When** it is converted to a Java record, **Then** all existing controller, service, and test references continue to compile and the serialisation behaviour is unchanged.
2. **Given** the converted DTO records have Jakarta Validation annotations on their fields, **When** a request is submitted with invalid data, **Then** validation errors are returned exactly as before conversion.
3. **Given** DTO classes that extend another DTO class, **When** the team reviews them, **Then** a documented decision is recorded explaining why they are kept as classes or redesigned to be records.

---

### User Story 2 - Properties Files Converted to YAML Format (Priority: P2)

A backend developer opens the Spring Boot configuration directory and finds YAML files (`application.yml`, `application-test.yml`) in place of the flat `.properties` files. The same configuration values are present, correctly structured, and the application behaves identically.

**Why this priority**: YAML configuration is hierarchical, more readable, and the Spring Boot community standard for multi-environment projects. Converting after DTO conversion keeps each change atomic and independently testable.

**Independent Test**: Start the application with `docker compose up` (or `./mvnw spring-boot:run`) and run the existing test suite with the `test` profile. If all tests pass and the application connects to the database, the conversion is verified.

**Acceptance Scenarios**:

1. **Given** `application.properties` exists, **When** it is replaced by an equivalent `application.yml`, **Then** the application starts successfully and all database and server settings take effect correctly.
2. **Given** `application-test.properties` exists, **When** it is replaced by `application-test.yml`, **Then** all existing tests pass with the `test` Spring profile active.
3. **Given** both YAML files are in place, **When** the old `.properties` files are removed, **Then** no other configuration source references the deleted files.

---

### Edge Cases

- What happens if a DTO class has a field with a default value or mutable state? (It cannot be a record; it stays as a class and is documented in the plan.)
- What if a DTO currently uses JavaBean-style setters relied on by a deserialisation library? (Records use canonical constructors; Jackson handles this transparently from Spring Boot 2.7+ with no additional configuration needed.)
- What happens to `OrderDetailResponse` and `ProductDetailResponse` which currently extend other DTOs? (Inheritance is incompatible with records; these classes are reviewed and either kept as classes or redesigned as flat records with all fields inlined.)
- What if the YAML migration introduces indentation or type-parsing errors? (The test suite catches these immediately; YAML numeric and boolean values must be unquoted, strings with special characters must be quoted.)

## Requirements *(mandatory)*

### Functional Requirements

**DTO Record Conversion**:

- **FR-001**: All DTO classes in `backend/src/main/java/com/petshop/dto/` that have no superclass (other than `Object`) and no subclasses MUST be converted to Java records.
- **FR-002**: All Jakarta Validation annotations (`@NotBlank`, `@Email`, `@Size`, `@Min`, `@Positive`, `@Pattern`) MUST be preserved on the record component parameters after conversion.
- **FR-003**: After conversion, all existing unit tests, integration tests, and controller tests MUST pass without modification to test logic.
- **FR-004**: DTO classes involved in inheritance relationships (`OrderSummaryResponse` / `OrderDetailResponse`, `ProductSummaryResponse` / `ProductDetailResponse`) MUST be reviewed; the outcome (keep as class or redesign as flat records) MUST be documented in `plan.md`.

**Properties-to-YAML Migration**:

- **FR-005**: `backend/src/main/resources/application.properties` MUST be replaced by an equivalent `application.yml` containing the same configuration keys and values in YAML format.
- **FR-006**: `backend/src/main/resources/application-test.properties` MUST be replaced by an equivalent `application-test.yml` containing the same configuration keys and values in YAML format.
- **FR-007**: After migration, the original `.properties` files MUST be deleted from the repository.
- **FR-008**: All configuration values MUST retain their original semantics; no values may be added, removed, or modified as part of this migration.

### Key Entities

- **DTO (Data Transfer Object)**: A class/record used exclusively to carry data between layers (controller ↔ service or API ↔ client). Has no behaviour beyond data access.
- **Java Record**: An immutable data class introduced in Java 16. Provides a canonical constructor, `equals`, `hashCode`, and `toString` automatically. Cannot be extended.
- **Spring Boot YAML Configuration**: A hierarchical configuration format supported natively by Spring Boot as an alternative to `.properties` files.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All eligible DTO classes (those with no superclass and no subclasses) are converted to records — zero boilerplate constructors, getters, `equals`, or `hashCode` remain in those classes.
- **SC-002**: The full backend test suite passes with a 100% pass rate after both the DTO record conversion and the YAML migration.
- **SC-003**: The application starts successfully against both the development PostgreSQL configuration and the test H2 configuration using the new YAML files.
- **SC-004**: Code review confirms no behavioural difference between the old and new configuration; only the file format changes.
- **SC-005**: A documented decision is on record for each DTO that could not be converted to a record, explaining the constraint (inheritance, mutable state, etc.).

## Assumptions

- The project uses Java 17 (or later), which supports records natively without preview flags.
- Spring Boot version in use supports records as Jackson-deserialised request bodies out of the box (Spring Boot 2.7+ with Jackson 2.14+).
- The existing test suite provides sufficient coverage to detect any regression introduced by the conversion.
- No external consumers (other services, code-generation tools) depend on the JavaBean property-accessor naming convention for the affected DTOs.
- YAML migration is a format-only change; no new environment variables or configuration profiles are introduced as part of this feature.
