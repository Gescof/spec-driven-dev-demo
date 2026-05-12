# Data Model: Pet Shop UI Redesign

**Feature**: 004-ui-redesign
**Phase**: 1 — Design

## Overview

This feature is a frontend-only visual redesign. No new data entities are introduced and no API contracts change. This document captures the **view model** — the data shape each new homepage section consumes and how it maps to existing API responses.

---

## View Models

### HeroSection

Static content — no API call.

| Field | Type | Value |
|-------|------|-------|
| `headline` | string | `"Treat Your Pet like royalty!"` |
| `ctaLabel` | string | `"SHOP NOW"` |
| `ctaHref` | string | `"catalog.html"` |
| `badge` | string | `"FREE DELIVERY"` |
| `heroImageSrc` | string | `"images/hero-pet.jpg"` (placeholder) |
| `heroImageAlt` | string | `"Happy golden retriever"` |

---

### NavigationBar

Partially dynamic — category links populated from API.

| Field | Type | Source |
|-------|------|--------|
| `brandName` | string | Static: `"Pet Store"` |
| `staticLinks` | `{label, href}[]` | Static: Home, Shop, Blog |
| `categoryLinks` | `{label, href}[]` | Computed from `GET /api/v1/categories`: label = `category.name`, href = `catalog.html?categoryId={id}` |
| `cartHref` | string | Static: `"cart.html"` |
| `accountHref` | string | Static: `"account.html"` (shown only when authenticated) |
| `cartCount` | integer | From existing cart badge logic in `auth.js` (unchanged) |

**Note**: The Dogs, Cats, Birds category links are resolved at runtime by matching the category names returned from `/api/v1/categories`. If a category is not found, the link is omitted gracefully.

---

### CategoriesSection

Sourced from `GET /api/v1/categories`.

| Field | Type | Source |
|-------|------|--------|
| `id` | integer | `Category.id` |
| `name` | string | `Category.name` |
| `imageSrc` | string | Static asset mapped by category name (see table below) |
| `imageAlt` | string | Computed: `"{name} category"` |
| `href` | string | Computed: `"catalog.html?categoryId={id}"` |

**Image mapping** (matched case-insensitively on `Category.name`):

| Keyword in name | Image file |
|-----------------|------------|
| `cat` | `images/cat.jpg` |
| `dog` | `images/dog.jpg` |
| `bird` | `images/bird.jpg` |
| `small` / `rabbit` / `rodent` | `images/small-pet.jpg` |
| *(no match)* | CSS background-color placeholder (`--color-card-warm`) |

**Display rule**: Render all categories from the API. If API returns zero categories, hide the section. If API call fails, hide the section silently (no error state shown on homepage).

---

### PopularProductsSection

Sourced from `GET /api/v1/products/featured` (returns up to 8 products).

| Field | Type | Source |
|-------|------|--------|
| `id` | integer | `ProductSummary.id` |
| `name` | string | `ProductSummary.name` |
| `price` | number | `ProductSummary.price` — formatted as USD currency |
| `imageSrc` | string \| null | `ProductSummary.imageUrl` (null → CSS placeholder) |
| `imageAlt` | string | Computed: `"{name} product image"` |
| `href` | string | Computed: `"product.html?id={id}"` |

**Display rule**: Show the first 4 products returned. If fewer than 4 are available, show what is returned. If the API call fails, show a subtle empty-state message.

---

### CustomerFavoritesSection

Static display section. No API call.

| Field | Type | Value |
|-------|------|-------|
| `images` | `{src, alt}[]` | 4 static pet images: `images/fav-1.jpg` … `images/fav-4.jpg` |

**Display rule**: Always shown (static content). No interactivity or navigation.

---

## API Endpoints Used (Unchanged)

| Endpoint | Method | Used By | Contract |
|----------|--------|---------|----------|
| `GET /api/v1/categories` | GET | NavigationBar, CategoriesSection | `Category[]` — see `specs/001-pet-shop-webapp/contracts/openapi.yaml` |
| `GET /api/v1/products/featured` | GET | PopularProductsSection | `ProductSummary[]` — see `specs/001-pet-shop-webapp/contracts/openapi.yaml` |

No new endpoints. No contract changes. No breaking changes to existing pages.
