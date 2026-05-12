# Quickstart: Verifying the DTO Records and YAML Config Migration

**Branch**: `003-dto-records-yaml-config` | **Date**: 2026-05-12

## Prerequisites

- Java 21 (verified: `java -version`)
- Maven 3.9+ (or use the Maven Wrapper: `./mvnw`)
- Docker + Docker Compose (optional — only needed for the full-stack smoke test)

---

## 1. Run the Backend Test Suite

After applying the DTO record conversion and YAML migration, verify all tests pass:

```bash
cd backend
./mvnw test
```

Expected output: `BUILD SUCCESS` with all tests passing. The test profile (`application-test.yml`) is activated automatically by Spring Boot Test.

---

## 2. Start the Application Locally (PostgreSQL required)

If you have a local PostgreSQL instance or are using Docker:

```bash
# Start only the database (from repo root)
docker compose up -d db

# Start the backend
cd backend
./mvnw spring-boot:run
```

The application reads `application.yml` on startup. Verify it connects by checking the log for:

```
Started PetShopApplication in ... seconds
```

---

## 3. Full Stack Smoke Test (Docker Compose)

```bash
docker compose up --build
```

1. Open the frontend URL (default: `http://localhost:5500`) in a browser.
2. Verify the home page loads and products are displayed.
3. Verify the API responds: `curl http://localhost:8080/api/products` should return a JSON array.

---

## 4. Verify JSON Serialisation is Unchanged

After conversion, spot-check one request and one response DTO via the API:

```bash
# Login (LoginRequest record)
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Products list (ProductSummaryResponse record)
curl -s http://localhost:8080/api/products | jq '.'
```

Confirm the JSON field names match the expected API contract (`id`, `name`, `price`, `imageUrl`, `available`, `category`).

---

## 5. Verify Validation Still Works

```bash
# Should return 400 with fieldErrors (ValidationErrorResponse record)
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"not-an-email","name":"","password":"short"}'
```

Expected response: HTTP 400 with `fieldErrors` map listing `email`, `name`, and `password` violations.

---

## What Changed

| Area | Before | After |
|------|--------|-------|
| DTO classes | 18 Java classes with manual getters/setters/constructors | 18 Java records with canonical constructors |
| `application.properties` | Flat key=value format | `application.yml` YAML hierarchy |
| `application-test.properties` | Flat key=value format | `application-test.yml` YAML hierarchy |
| API JSON output | Unchanged | Unchanged |
| Test suite | Unchanged | Unchanged (all tests pass) |
