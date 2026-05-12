# Tasks: DTO Records and YAML Config

**Input**: Design documents from `specs/003-dto-records-yaml-config/`
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ quickstart.md ✅

**Tests**: No new test tasks — this is a structural refactoring with no behavioural change. The existing test suite is the regression guard. Tests are run at checkpoints after each conversion group.

**Organization**: Tasks are grouped by user story. Each group is independently verifiable by running `cd backend && ./mvnw test`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (each task touches a different file)
- **[Story]**: Which user story this task belongs to (US1 = DTO conversion, US2 = YAML migration)
- Exact file paths are included in all descriptions

---

## Phase 1: Setup (Baseline Verification)

**Purpose**: Confirm the build compiles and all tests pass before any changes are made.

- [ ] T001 Run `cd backend && ./mvnw test` and confirm `BUILD SUCCESS` — establishes the pass/fail baseline before conversion begins

---

## Phase 2: Foundational (Blocking Prerequisites)

> **N/A for this feature.** This is a refactoring with no new infrastructure, no new shared models, and no new configuration that must precede story work. Proceed directly to Phase 3.

---

## Phase 3: User Story 1 — DTO Record Conversion (Priority: P1) 🎯 MVP

**Goal**: Replace all 18 DTO classes in `backend/src/main/java/com/petshop/dto/` with Java records, eliminating boilerplate constructors, getters, setters, and `equals`/`hashCode`. The inheritance hierarchy (`OrderDetailResponse extends OrderSummaryResponse` and `ProductDetailResponse extends ProductSummaryResponse`) is eliminated by flattening each class into a self-contained flat record.

**Independent Test**: Run `cd backend && ./mvnw test` — all tests pass. The application starts successfully with `./mvnw spring-boot:run`.

### Implementation for User Story 1

#### Group A: Request DTOs (no superclass, no subclasses — fully parallel)

- [ ] T002 [P] [US1] Convert `AddToCartRequest` to a Java record: replace class body with `public record AddToCartRequest(@Positive Long productId, @Min(1) int quantity) {}` in `backend/src/main/java/com/petshop/dto/AddToCartRequest.java`
- [ ] T003 [P] [US1] Convert `LoginRequest` to a Java record: replace class body with `public record LoginRequest(String email, String password) {}` in `backend/src/main/java/com/petshop/dto/LoginRequest.java`
- [ ] T004 [P] [US1] Convert `RegisterRequest` to a Java record: replace class body with `public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(max=200) String name, @NotBlank @Size(min=8) String password) {}` in `backend/src/main/java/com/petshop/dto/RegisterRequest.java`
- [ ] T005 [P] [US1] Convert `PlaceOrderRequest` to a Java record: replace class body with `public record PlaceOrderRequest(String cardholderName, @Pattern(regexp="\\d{4}") String cardNumberLast4) {}` in `backend/src/main/java/com/petshop/dto/PlaceOrderRequest.java`
- [ ] T006 [P] [US1] Convert `UpdateCartItemRequest` to a Java record: replace class body with `public record UpdateCartItemRequest(@Min(1) int quantity) {}` in `backend/src/main/java/com/petshop/dto/UpdateCartItemRequest.java`
- [ ] T007 [P] [US1] Convert `UpdateProfileRequest` to a Java record: replace class body with `public record UpdateProfileRequest(@Size(min=1) String name, String currentPassword, @Size(min=8) String newPassword) {}` in `backend/src/main/java/com/petshop/dto/UpdateProfileRequest.java`

#### Group B: Leaf Response DTOs (no superclass, no subclasses — fully parallel)

- [ ] T008 [P] [US1] Convert `CategoryResponse` to a Java record: replace class body with `public record CategoryResponse(Long id, String name, String description) {}` in `backend/src/main/java/com/petshop/dto/CategoryResponse.java`
- [ ] T009 [P] [US1] Convert `OrderLineItemResponse` to a Java record: replace class body with `public record OrderLineItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}` in `backend/src/main/java/com/petshop/dto/OrderLineItemResponse.java`
- [ ] T010 [P] [US1] Convert `CartItemResponse` to a Java record: replace class body with `public record CartItemResponse(Long productId, String productName, String productImageUrl, BigDecimal unitPrice, int quantity, BigDecimal lineTotal, boolean available) {}` in `backend/src/main/java/com/petshop/dto/CartItemResponse.java`
- [ ] T011 [P] [US1] Convert `ErrorResponse` to a Java record: replace class body with `public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}` in `backend/src/main/java/com/petshop/dto/ErrorResponse.java`; verify `GlobalExceptionHandler` in `backend/src/main/java/com/petshop/GlobalExceptionHandler.java` still compiles (constructor call signature is unchanged)
- [ ] T012 [P] [US1] Convert `UserProfileResponse` to a Java record: replace class body with `public record UserProfileResponse(Long id, String email, String name, LocalDateTime registeredAt) {}` in `backend/src/main/java/com/petshop/dto/UserProfileResponse.java`
- [ ] T013 [P] [US1] Convert `ValidationErrorResponse` to a Java record: replace class body with `public record ValidationErrorResponse(int status, String error, String message, LocalDateTime timestamp, Map<String,String> fieldErrors) {}` in `backend/src/main/java/com/petshop/dto/ValidationErrorResponse.java`; verify `GlobalExceptionHandler` in `backend/src/main/java/com/petshop/GlobalExceptionHandler.java` still compiles

#### Group C: Composite Response DTOs (depend only on leaf records — parallel with each other after Group B)

- [ ] T014 [P] [US1] Convert `CartResponse` to a Java record: replace class body with `public record CartResponse(List<CartItemResponse> items, BigDecimal grandTotal, int itemCount) {}` in `backend/src/main/java/com/petshop/dto/CartResponse.java`
- [ ] T015 [P] [US1] Convert `ProductPageResponse` to a Java record: replace class body with `public record ProductPageResponse(List<ProductSummaryResponse> content, long totalElements, int totalPages, int page, int size) {}` in `backend/src/main/java/com/petshop/dto/ProductPageResponse.java` (this will compile once ProductSummaryResponse is converted in T017)

#### Group D: Inheritance Hierarchy Flattening (sequential — each pair must be done together)

- [ ] T016 [US1] Convert `OrderSummaryResponse` to a flat Java record — remove the class, delete the `extends` relationship, and replace with `public record OrderSummaryResponse(Long id, String orderNumber, String status, LocalDateTime placedAt, BigDecimal totalAmount, int itemCount) {}` in `backend/src/main/java/com/petshop/dto/OrderSummaryResponse.java`
- [ ] T017 [US1] Convert `OrderDetailResponse` to a flat Java record — remove `extends OrderSummaryResponse`, inline all six parent fields plus `items`, and replace with `public record OrderDetailResponse(Long id, String orderNumber, String status, LocalDateTime placedAt, BigDecimal totalAmount, int itemCount, List<OrderLineItemResponse> items) {}` in `backend/src/main/java/com/petshop/dto/OrderDetailResponse.java`; update any `super(...)` construction sites in `backend/src/main/java/com/petshop/service/OrderService.java` to pass all seven fields directly to the canonical constructor
- [ ] T018 [US1] Convert `ProductSummaryResponse` to a flat Java record — remove the class, delete the `extends` relationship, and replace with `public record ProductSummaryResponse(Long id, String name, BigDecimal price, String imageUrl, boolean available, CategoryResponse category) {}` in `backend/src/main/java/com/petshop/dto/ProductSummaryResponse.java`
- [ ] T019 [US1] Convert `ProductDetailResponse` to a flat Java record — remove `extends ProductSummaryResponse`, inline all six parent fields plus `description`, and replace with `public record ProductDetailResponse(Long id, String name, BigDecimal price, String imageUrl, boolean available, CategoryResponse category, String description) {}` in `backend/src/main/java/com/petshop/dto/ProductDetailResponse.java`; update any `super(...)` construction sites in `backend/src/main/java/com/petshop/service/ProductService.java` to pass all seven fields directly to the canonical constructor

#### Checkpoint: User Story 1

- [ ] T020 [US1] Run `cd backend && ./mvnw test` and confirm `BUILD SUCCESS` with all tests passing — all 18 DTO records are in place and the existing test suite validates correctness

**At this checkpoint, User Story 1 is fully complete and independently testable.**

---

## Phase 4: User Story 2 — YAML Configuration Migration (Priority: P2)

**Goal**: Replace both `.properties` files with equivalent `.yml` files and delete the originals. The application behaviour is identical; only the configuration file format changes.

**Independent Test**: Run `cd backend && ./mvnw test` with the new YAML files in place. Confirm `BUILD SUCCESS`. Optionally start the application with `./mvnw spring-boot:run` and verify it connects to the database.

### Implementation for User Story 2

- [ ] T021 [US2] Create `backend/src/main/resources/application.yml` with the following content (exact YAML from plan.md):

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

- [ ] T022 [US2] Delete `backend/src/main/resources/application.properties` — confirm the file no longer exists in the repository
- [ ] T023 [US2] Create `backend/src/test/resources/application-test.yml` with the following content (exact YAML from plan.md):

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

- [ ] T024 [US2] Delete `backend/src/test/resources/application-test.properties` — confirm the file no longer exists in the repository
- [ ] T025 [US2] Run `cd backend && ./mvnw test` and confirm `BUILD SUCCESS` with all tests passing using the new YAML configuration files

**At this checkpoint, User Story 2 is fully complete and independently testable.**

---

## Final Phase: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and documentation check across both user stories.

- [ ] T026 [P] Run full test suite one final time: `cd backend && ./mvnw test` — confirm `BUILD SUCCESS` with 100% pass rate as the definitive acceptance sign-off
- [ ] T027 [P] Verify application starts against Docker database: `docker compose up -d db && cd backend && ./mvnw spring-boot:run` — confirm startup log shows `Started PetShopApplication` with no errors
- [ ] T028 Spot-check JSON serialisation via `curl`: confirm `POST /api/auth/login` and `GET /api/products` return the expected JSON shapes (field names and types match the existing API contract)
- [ ] T029 Confirm no `.properties` files remain: `ls backend/src/main/resources/` and `ls backend/src/test/resources/` should show only `.yml` files

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — run immediately
- **Phase 3 (US1)**: Depends on Phase 1 passing (baseline confirmed)
  - Groups A and B: All parallel — each task touches a different file
  - Group C: Parallel with Groups A and B; `ProductPageResponse` (T015) imports `ProductSummaryResponse` so it will only compile cleanly after T018
  - Group D: Sequential within each pair (T016→T017, T018→T019); the two pairs are parallel with each other
- **Phase 4 (US2)**: Depends on Phase 3 checkpoint (T020 passing)
  - T021 and T023 are parallel (different files)
  - T022 and T024 follow T021 and T023 respectively
  - T025 requires T021–T024 complete
- **Final Phase**: Depends on T025 passing

### User Story Dependencies

- **US1 (P1)**: Starts after Phase 1 — no dependency on US2
- **US2 (P2)**: Starts after US1 checkpoint (T020) — configuration change is cleanest after the Java layer is stable

### Parallel Opportunities Within US1

- T002–T013: All 12 tasks touch different files — run all in parallel
- T014–T015: Parallel with each other and with Groups A/B
- T016 and T018: Parallel with each other (different files)
- T017 and T019: Parallel with each other after their respective parents (T016 and T018)

---

## Parallel Example: User Story 1 (Group A + B)

```bash
# Launch all request and leaf-response DTO conversions simultaneously:
Task: T002 — AddToCartRequest.java
Task: T003 — LoginRequest.java
Task: T004 — RegisterRequest.java
Task: T005 — PlaceOrderRequest.java
Task: T006 — UpdateCartItemRequest.java
Task: T007 — UpdateProfileRequest.java
Task: T008 — CategoryResponse.java
Task: T009 — OrderLineItemResponse.java
Task: T010 — CartItemResponse.java
Task: T011 — ErrorResponse.java
Task: T012 — UserProfileResponse.java
Task: T013 — ValidationErrorResponse.java
Task: T014 — CartResponse.java

# After the above, run inheritance-hierarchy pairs in parallel:
Task: T016 — OrderSummaryResponse.java (then T017)
Task: T018 — ProductSummaryResponse.java (then T019)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Baseline (`./mvnw test` → BUILD SUCCESS)
2. Convert all 18 DTOs (Phase 3, in dependency order)
3. **STOP and VALIDATE**: `./mvnw test` → BUILD SUCCESS (T020)
4. **Demo**: Application starts and all endpoints respond correctly

### Incremental Delivery

1. Baseline → confirm green
2. Convert Groups A+B (12 DTOs) → run tests → green
3. Convert Group C (CartResponse, ProductPageResponse) → run tests → green
4. Convert Group D (4 inheritance DTOs) → run tests → green (US1 complete)
5. Migrate `application.yml` → run tests → green
6. Migrate `application-test.yml` → delete `.properties` files → run tests → green (US2 complete)

---

## Notes

- **[P]** tasks touch different files and have no shared dependency — safe to execute concurrently
- Records use the canonical constructor for all instantiation; no setter calls remain after conversion
- The H2 JDBC URL in `application-test.yml` MUST be quoted (`"..."`) — the semicolons are valid YAML string characters only inside quotes
- Run `./mvnw test` after each group, not just at the end — this keeps the build green throughout
- If any task reveals a setter-based construction site not covered above, update the service caller in the same task before marking it complete
