# Data Model: DTO Records and YAML Config

**Branch**: `003-dto-records-yaml-config` | **Date**: 2026-05-12

> This feature is a refactoring — it introduces no new data entities and makes no changes to the JPA entity model or the database schema. This document catalogues the structural changes to the DTO layer only.

---

## DTO Conversion Inventory

### Category: Directly Convertible (no inheritance)

All 14 classes below have no superclass (other than `Object`) and no subclasses. Each becomes a Java record with component parameters matching the current fields.

| Class | Package | Components | Validation annotations |
|-------|---------|-----------|----------------------|
| `AddToCartRequest` | `dto` | `productId: Long`, `quantity: int` | `@Positive` on `productId`, `@Min(1)` on `quantity` |
| `LoginRequest` | `dto` | `email: String`, `password: String` | None |
| `RegisterRequest` | `dto` | `email: String`, `name: String`, `password: String` | `@NotBlank @Email` on `email`, `@NotBlank @Size(max=200)` on `name`, `@NotBlank @Size(min=8)` on `password` |
| `PlaceOrderRequest` | `dto` | `cardholderName: String`, `cardNumberLast4: String` | `@Pattern(regexp="\\d{4}")` on `cardNumberLast4` |
| `UpdateCartItemRequest` | `dto` | `quantity: int` | `@Min(1)` on `quantity` |
| `UpdateProfileRequest` | `dto` | `name: String`, `currentPassword: String`, `newPassword: String` | `@Size(min=1)` on `name`, `@Size(min=8)` on `newPassword` |
| `CartItemResponse` | `dto` | `productId: Long`, `productName: String`, `productImageUrl: String`, `unitPrice: BigDecimal`, `quantity: int`, `lineTotal: BigDecimal`, `available: boolean` | None |
| `CartResponse` | `dto` | `items: List<CartItemResponse>`, `grandTotal: BigDecimal`, `itemCount: int` | None |
| `CategoryResponse` | `dto` | `id: Long`, `name: String`, `description: String` | None |
| `ErrorResponse` | `dto` | `status: int`, `error: String`, `message: String`, `timestamp: LocalDateTime` | None |
| `OrderLineItemResponse` | `dto` | `productId: Long`, `productName: String`, `unitPrice: BigDecimal`, `quantity: int`, `lineTotal: BigDecimal` | None |
| `ProductPageResponse` | `dto` | `content: List<ProductSummaryResponse>`, `totalElements: long`, `totalPages: int`, `page: int`, `size: int` | None |
| `UserProfileResponse` | `dto` | `id: Long`, `email: String`, `name: String`, `registeredAt: LocalDateTime` | None |
| `ValidationErrorResponse` | `dto` | `status: int`, `error: String`, `message: String`, `timestamp: LocalDateTime`, `fieldErrors: Map<String,String>` | None |

---

### Category: Inheritance Hierarchy — Flattened to Records

The four classes below currently use class inheritance to share fields. Since records are `final` and cannot be extended, the inheritance is eliminated by flattening each type into a self-contained record. The parent fields are inlined into the child record.

#### Before (class hierarchy)

```
OrderSummaryResponse          (6 fields: id, orderNumber, status, placedAt, totalAmount, itemCount)
└── OrderDetailResponse       (inherits 6 + adds: items)

ProductSummaryResponse        (6 fields: id, name, price, imageUrl, available, category)
└── ProductDetailResponse     (inherits 6 + adds: description)
```

#### After (independent flat records)

| Record | Components |
|--------|-----------|
| `OrderSummaryResponse` | `id: Long`, `orderNumber: String`, `status: String`, `placedAt: LocalDateTime`, `totalAmount: BigDecimal`, `itemCount: int` |
| `OrderDetailResponse` | `id: Long`, `orderNumber: String`, `status: String`, `placedAt: LocalDateTime`, `totalAmount: BigDecimal`, `itemCount: int`, `items: List<OrderLineItemResponse>` |
| `ProductSummaryResponse` | `id: Long`, `name: String`, `price: BigDecimal`, `imageUrl: String`, `available: boolean`, `category: CategoryResponse` |
| `ProductDetailResponse` | `id: Long`, `name: String`, `price: BigDecimal`, `imageUrl: String`, `available: boolean`, `category: CategoryResponse`, `description: String` |

**Impact on callers**: Service methods that construct `OrderDetailResponse` and `ProductDetailResponse` currently call `super(...)` then set additional fields. After flattening, the canonical record constructor receives all fields in one call. This is a mechanical update — the values passed are unchanged.

---

## Configuration File Changes

### application.properties → application.yml

**Location**: `backend/src/main/resources/`

| Property key | Old value | YAML representation |
|-------------|-----------|-------------------|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/petshop` | under `spring.datasource.url` |
| `spring.datasource.username` | `petshop_user` | under `spring.datasource.username` |
| `spring.datasource.password` | `petshop_pass` | under `spring.datasource.password` |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | under `spring.jpa.hibernate.ddl-auto` |
| `spring.jpa.show-sql` | `false` | under `spring.jpa.show-sql` |
| `server.port` | `8080` | under `server.port` |
| `petshop.cors.allowed-origins` | `http://localhost:5500,http://127.0.0.1:5500` | under `petshop.cors.allowed-origins` |

### application-test.properties → application-test.yml

**Location**: `backend/src/test/resources/`

| Property key | Old value | Notes |
|-------------|-----------|-------|
| `spring.datasource.url` | `jdbc:h2:mem:petshop_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL` | Must be quoted in YAML (contains `;`) |
| `spring.datasource.driver-class-name` | `org.h2.Driver` | Standard YAML string |
| `spring.datasource.username` | `sa` | Standard YAML string |
| `spring.datasource.password` | *(empty)* | Represented as `""` in YAML |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Standard YAML string |
| `spring.jpa.show-sql` | `false` | YAML boolean (unquoted) |
| `spring.security.user.name` | `test` | Standard YAML string |
| `spring.security.user.password` | `test` | Standard YAML string |
| `petshop.cors.allowed-origins` | `http://localhost:5500` | Standard YAML string |

---

## JSON Contract Compatibility

**No breaking changes.** The JSON serialised output of all converted records is identical to the previous class-based output:

- Field names: unchanged (derived from component names, same as old getter names minus `get`/`is` prefix)
- Field types: unchanged
- Null handling: unchanged (Jackson serialises `null` components as `null` JSON values by default)
- Validation error responses: unchanged (same field names in the JSON body)

The `contracts/openapi.yaml` produced for `001-pet-shop-webapp` remains valid and authoritative. No contract version increment is required.
