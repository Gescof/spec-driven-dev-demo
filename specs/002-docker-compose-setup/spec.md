# Feature Specification: Docker Compose Setup

**Feature Branch**: `002-docker-compose-setup`  
**Created**: 2026-05-11  
**Status**: Draft  
**Input**: User description: "Add Docker build (compose) for both BE (including DB) and FE to run the whole app"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Run the Full App with One Command (Priority: P1)

A developer clones the repository and starts the entire pet shop application — backend API, PostgreSQL database, and frontend — with a single `docker compose up` command, without installing Java, Node, or PostgreSQL locally.

**Why this priority**: This is the entire value of the feature. Without it, nothing else matters. It is also a self-contained deliverable that proves the containerisation works end-to-end.

**Independent Test**: Can be fully tested by running `docker compose up` on a clean machine (Docker only), navigating to the frontend URL, and verifying the home page loads and the API responds.

**Acceptance Scenarios**:

1. **Given** Docker is installed and the repository is cloned, **When** the developer runs `docker compose up`, **Then** all three services (database, backend, frontend) start successfully with no manual configuration required beyond copying `.env.example` to `.env`.
2. **Given** the application is running, **When** the developer opens the frontend URL in a browser, **Then** the pet shop home page loads and displays products fetched from the backend API.
3. **Given** the application is running, **When** the developer stops it with `docker compose down`, **Then** all containers stop cleanly and no data corruption occurs.
4. **Given** the application is running and the developer stops and restarts it, **When** the database container restarts, **Then** previously created data (users, orders) is preserved via a named volume.

---

### User Story 2 - Backend and Database Run Together (Priority: P2)

A backend developer wants to work on the API without running the frontend. They start only the database and backend services using Docker Compose service selection, connect their IDE debugger, and use the API directly.

**Why this priority**: Allows backend-only development and debugging without the full stack. Useful for running backend tests against a real database in CI.

**Independent Test**: Can be tested by running `docker compose up db backend`, sending HTTP requests to the backend port, and verifying correct API responses including database reads/writes.

**Acceptance Scenarios**:

1. **Given** the developer runs `docker compose up db backend`, **When** both containers are healthy, **Then** the backend API is reachable on its published port and can read from and write to the database.
2. **Given** the backend container starts, **When** the database is not yet ready, **Then** the backend retries the connection and only serves traffic once the database health check passes.

---

### User Story 3 - Frontend Served via Nginx in Container (Priority: P3)

A developer or reviewer wants to preview the frontend exactly as it would appear in production — served by Nginx from a container rather than a local dev server — to catch any path or header differences.

**Why this priority**: Validates the frontend Dockerfile and Nginx configuration. Lower priority because the core value (full stack up) is delivered by P1.

**Independent Test**: Can be tested by running `docker compose up frontend`, verifying static files are served correctly, and confirming that API requests are proxied to the backend without CORS errors.

**Acceptance Scenarios**:

1. **Given** the frontend container is running, **When** a browser requests the home page URL, **Then** `index.html` is served and all linked CSS and JS files load without errors.
2. **Given** the frontend container is running alongside the backend, **When** a browser makes an API call through the frontend, **Then** the request is correctly proxied to the backend and the response is returned without CORS errors.

---

### Edge Cases

- What happens if Docker is not installed? (Clear error from Docker CLI; a prerequisite note in `quickstart.md` explains the requirement.)
- What happens if the required ports are already in use on the host? (Docker reports a bind error; the `.env` file allows port remapping.)
- What happens if the `.env` file is missing? (Compose uses defaults from `docker-compose.yml`; the README warns that the defaults are for local development only.)
- What happens if the database volume already contains data from a previous schema version? (Developer must manually remove the volume with `docker compose down -v` and re-seed; documented in `quickstart.md`.)
- What happens during a build if the backend source fails to compile? (Docker build fails at the Maven compile step with a clear error; no broken image is pushed.)

## Requirements *(mandatory)*

### Functional Requirements

**Docker Images**:

- **FR-001**: System MUST provide a `backend/Dockerfile` that builds a runnable backend image using a multi-stage build (compile/package stage → minimal runtime stage).
- **FR-002**: System MUST provide a `frontend/Dockerfile` that packages the static frontend files into an Nginx-based image.
- **FR-003**: Both Dockerfiles MUST produce images that run as non-root users.

**Docker Compose Orchestration**:

- **FR-004**: System MUST provide a `docker-compose.yml` at the repository root that defines three services: `db`, `backend`, and `frontend`.
- **FR-005**: The `db` service MUST use the official PostgreSQL 15 image and persist data via a named Docker volume.
- **FR-006**: The `backend` service MUST declare a health-check dependency on `db` so it only starts after the database is accepting connections.
- **FR-007**: The `frontend` service MUST proxy API requests to the `backend` service so the browser never communicates with the backend directly across different origins.
- **FR-008**: All configurable values (database credentials, ports, API base URL) MUST be externalised to environment variables with safe defaults defined in the Compose file.

**Developer Experience**:

- **FR-009**: System MUST provide an `.env.example` file documenting all required environment variables with example values.
- **FR-010**: System MUST provide a `quickstart.md` (or update the existing one) documenting how to build and run the full application with Docker Compose.
- **FR-011**: The full application MUST be startable with `docker compose up` (or `docker compose up --build` on first run) without any other prerequisite beyond Docker being installed.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer with only Docker installed can have the full application running within 5 minutes of cloning the repository (excluding image download time).
- **SC-002**: All three services start successfully and pass their health checks 100% of the time on `docker compose up` when no port conflicts exist.
- **SC-003**: The frontend home page loads and displays data fetched from the containerised backend within 5 seconds of all containers reporting healthy.
- **SC-004**: Data written to the database survives a `docker compose down` followed by `docker compose up` (volume persistence verified).
- **SC-005**: No hardcoded credentials or environment-specific values appear in any Dockerfile or `docker-compose.yml`; all are supplied via environment variables or `.env`.

## Assumptions

- Docker Desktop (or Docker Engine + Compose plugin) is the only prerequisite; no Java, Maven, Python, or Node.js installation is required on the host.
- The application is containerised for local development and demo use only; production hardening (secrets management, multi-replica, load balancing) is out of scope.
- The backend build inside Docker uses Maven (matching the `pom.xml` in `backend/`); no pre-built JAR is assumed to exist.
- The frontend consists of static HTML, CSS, and JavaScript files (no build step required); Nginx serves them directly.
- Nginx in the frontend container proxies `/api/` requests to the `backend` service using Docker's internal DNS; no external reverse proxy is needed.
- The PostgreSQL database is initialised with the schema automatically by the Spring Boot application on startup (`ddl-auto`); a separate init SQL script may be used for seed data.
- A single `.env` file at the repository root is sufficient for all environment configuration.
