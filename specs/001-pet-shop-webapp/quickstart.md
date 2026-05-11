# Quickstart: Pet Shop Web App

**Branch**: `001-pet-shop-webapp` | **Date**: 2026-05-11

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java | 21 (LTS) | `java -version` |
| Maven | 3.9+ | Bundled wrapper (`./mvnw`) |
| PostgreSQL | 15+ | Must be running locally on port 5432 |

## 1. Database Setup

```sql
-- Run as a PostgreSQL superuser (e.g., psql -U postgres)
CREATE DATABASE petshop;
CREATE USER petshop_user WITH PASSWORD 'petshop_pass';
GRANT ALL PRIVILEGES ON DATABASE petshop TO petshop_user;
```

## 2. Backend Setup

```bash
cd backend

# Copy and edit the application properties
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Edit `application.properties` to match your local database:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/petshop
spring.datasource.username=petshop_user
spring.datasource.password=petshop_pass
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

server.port=8080

# CORS: allow the frontend dev origin
petshop.cors.allowed-origins=http://localhost:5500,http://127.0.0.1:5500
```

## 3. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080/api/v1`.

Verify:
```bash
curl http://localhost:8080/api/v1/categories
# Expected: [] (empty array until seed data is loaded)
```

## 4. Run the Frontend

The frontend is plain HTML/CSS/JS — no build step required.

**Option A — VS Code Live Server** (recommended):
1. Open the `frontend/` folder in VS Code.
2. Right-click `index.html` → **Open with Live Server**.
3. Browser opens at `http://127.0.0.1:5500`.

**Option B — Python simple server**:
```bash
cd frontend
python -m http.server 5500
# Open http://localhost:5500 in your browser
```

**Option C — Open directly**:
Open `frontend/index.html` in your browser (note: `file://` CORS restrictions may apply for fetch calls — prefer options A or B).

## 5. Seed Data (optional)

To populate the database with sample categories and products, run the seed SQL script after starting the backend:

```bash
psql -U petshop_user -d petshop -f backend/src/main/resources/db/seed.sql
```

## 6. Running Tests

```bash
cd backend
./mvnw test
```

Tests require the PostgreSQL database to be running. The test profile uses a separate schema (`petshop_test`) and `ddl-auto=create-drop`.

## Project Layout

```text
spec-driven-dev-demo/
├── backend/                  # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/petshop/
│   │   │   │   ├── config/       # Security, CORS, web config
│   │   │   │   ├── controller/   # REST controllers (@RestController)
│   │   │   │   ├── dto/          # Request/response DTOs
│   │   │   │   ├── model/        # JPA entities
│   │   │   │   ├── repository/   # Spring Data JPA repositories
│   │   │   │   ├── service/      # Business logic
│   │   │   │   └── PetShopApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/
│   │   │           └── seed.sql
│   │   └── test/
│   │       └── java/com/petshop/
│   │           ├── controller/   # MockMvc controller tests
│   │           ├── service/      # Unit tests
│   │           └── integration/  # Full-stack integration tests
│   └── pom.xml
│
├── frontend/                 # Vanilla HTML/CSS/JS
│   ├── index.html            # Home page
│   ├── catalog.html          # Product catalog (filter + search)
│   ├── product.html          # Product detail
│   ├── login.html            # Login form
│   ├── register.html         # Registration form
│   ├── account.html          # User profile + order history
│   ├── cart.html             # Shopping cart
│   ├── checkout.html         # Simulated checkout
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── api.js            # Shared fetch() wrapper (base URL, credentials)
│       ├── auth.js           # Session state helpers (redirect if not logged in)
│       ├── catalog.js
│       ├── product.js
│       ├── account.js
│       ├── cart.js
│       └── checkout.js
│
└── specs/
    └── 001-pet-shop-webapp/  # This feature's design artifacts
```

## API Reference

See [contracts/openapi.yaml](contracts/openapi.yaml) for the full OpenAPI 3.0 specification.

Base URL: `http://localhost:8080/api/v1`

All authenticated endpoints require an active session (JSESSIONID cookie). The frontend sets `credentials: 'include'` on all fetch calls.

## Common Issues

| Problem | Fix |
|---|---|
| `Connection refused` on backend start | Verify PostgreSQL is running: `pg_isready -h localhost -p 5432` |
| CORS error in browser | Confirm `petshop.cors.allowed-origins` includes your frontend origin |
| `ddl-auto` drops data on restart | Expected for development; add seed.sql re-run or switch to `update` mode |
| 401 on authenticated endpoints | Session expired; log in again at `POST /api/v1/auth/login` |
