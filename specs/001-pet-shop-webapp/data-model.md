# Data Model: Pet Shop Web App

**Branch**: `001-pet-shop-webapp` | **Date**: 2026-05-11 | **Spec**: [spec.md](spec.md)

## Entities

### Category

Grouping of related products (e.g., Dogs, Cats, Birds, Fish).

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-increment |
| name | String | NOT NULL, UNIQUE, max 100 chars |
| description | String | Nullable, text |

**Relationships**: One Category → many Products.

---

### Product

An item available for purchase.

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-increment |
| name | String | NOT NULL, max 200 chars |
| description | String | NOT NULL, text |
| price | BigDecimal | NOT NULL, precision 10 scale 2, > 0 |
| category | Category | NOT NULL, FK → category.id |
| imageUrl | String | Nullable, max 500 chars (external URL; no upload per spec) |
| available | Boolean | NOT NULL, default true |
| createdAt | LocalDateTime | NOT NULL, auto-set on insert |

**Relationships**: Many Products → one Category.

**Validation rules**:
- `price` must be > 0.00
- `name` must not be blank
- `imageUrl`, if present, must be a valid URL format

---

### User

A registered customer.

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-increment |
| email | String | NOT NULL, UNIQUE, max 255 chars, valid email format |
| name | String | NOT NULL, max 200 chars |
| passwordHash | String | NOT NULL (BCrypt; never plain text) |
| registeredAt | LocalDateTime | NOT NULL, auto-set on insert |

**Relationships**:
- One User → one Cart (created lazily on first add-to-cart action).
- One User → many Orders.

**Validation rules**:
- `email` must match standard RFC-compliant email format
- Raw password (pre-hash) must be ≥ 8 characters
- `name` must not be blank

---

### Cart

A persistent, user-owned collection of items intended for purchase.

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-increment |
| user | User | NOT NULL, UNIQUE FK → user.id (OneToOne) |
| createdAt | LocalDateTime | NOT NULL, auto-set on insert |
| updatedAt | LocalDateTime | NOT NULL, updated on each modification |

**Relationships**: One Cart → many CartItems.

---

### CartItem

A single product entry within a user's cart.

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-increment |
| cart | Cart | NOT NULL, FK → cart.id |
| product | Product | NOT NULL, FK → product.id |
| quantity | Integer | NOT NULL, ≥ 1 |

**Unique constraint**: `(cart_id, product_id)` — only one CartItem per product per cart. Adding the same product increments quantity.

---

### Order

A completed purchase record.

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-increment |
| orderNumber | String | NOT NULL, UNIQUE (UUID-based, human-readable prefix: `ORD-{uuid}`) |
| user | User | NOT NULL, FK → user.id |
| status | OrderStatus | NOT NULL, enum: `CONFIRMED` (v1 only) |
| placedAt | LocalDateTime | NOT NULL, auto-set on insert |
| totalAmount | BigDecimal | NOT NULL, precision 10 scale 2 (computed at placement from OrderItems) |

**Relationships**: One Order → many OrderItems.

**State transitions (v1)**:
```
cart checkout → CONFIRMED
```
Full lifecycle (PENDING → SHIPPED → DELIVERED → CANCELLED) is out of scope for v1.

---

### OrderItem

A price-and-name snapshot of one product line in a completed order.

| Field | Type | Constraints |
|---|---|---|
| id | Long | PK, auto-increment |
| order | Order | NOT NULL, FK → order.id |
| product | Product | NOT NULL, FK → product.id |
| productName | String | NOT NULL, max 200 (snapshot of Product.name at order time) |
| unitPrice | BigDecimal | NOT NULL, precision 10 scale 2 (snapshot of Product.price at order time) |
| quantity | Integer | NOT NULL, ≥ 1 |

**Why snapshots**: Product price and name can change after an order is placed. FR-018 requires that order history reflects values at the time of purchase. Snapshots isolate order records from future catalog changes.

---

## Entity Relationship Diagram

```
Category (1) ──────────────< Product (N)
                                 │
User (1) ──── Cart (1) ──< CartItem (N) >────── Product (1)
   │
   └──< Order (N) ──< OrderItem (N) >────────── Product (1)
```

## Database Configuration Notes

- All timestamps use `LocalDateTime`; application layer normalizes to UTC.
- No hard deletes: Products set `available = false` to hide from catalog; Users and Orders are never deleted in v1.
- `spring.jpa.hibernate.ddl-auto=create-drop` for local development (schema recreated on startup — acceptable for demo).
- Unique constraint on `(cart_id, product_id)` enforced at both DB and service layers.
- BCrypt work factor: Spring Security default (10 rounds); no override needed for v1.
