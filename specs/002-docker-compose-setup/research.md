# Research: Docker Compose Setup

**Feature**: 002-docker-compose-setup  
**Date**: 2026-05-11

## Decision 1: Backend Base Image Strategy

**Decision**: Multi-stage build using `eclipse-temurin:21-jdk-alpine` (build) → `eclipse-temurin:21-jre-alpine` (runtime).

**Rationale**: The build stage needs the full JDK and Maven wrapper to compile and package the Spring Boot JAR. The runtime stage needs only the JRE to execute the fat JAR. Splitting stages reduces the final image from ~600 MB (JDK image) to ~220 MB (JRE image), and no source code or build tools are present in the distributed image.

**Alternatives considered**:
- **Single JDK stage**: Simpler Dockerfile but distributes the full JDK and source tree — rejected (unnecessary size, security surface).
- **Distroless image**: Smaller and more secure but lacks shell access for debugging; rejected for a demo/development context where shell access aids troubleshooting.
- **Pre-built JAR (no Docker build)**: Would require Java installed on the host to build first — rejected (violates the "Docker only" requirement from FR-011).

---

## Decision 2: Frontend Container Strategy

**Decision**: `nginx:1.25-alpine` serving static HTML/CSS/JS files directly; a custom `nginx.conf` proxies `/api/` requests to the `backend` service using Docker's internal DNS.

**Rationale**: The frontend is vanilla HTML/CSS/JS with no build step, so no Node.js or build tooling is needed in the image. Nginx is the standard choice for serving static files: small Alpine image (~8 MB), high performance, and built-in reverse proxy support. The proxy configuration eliminates CORS issues without requiring changes to the Spring Boot CORS config in development.

**Alternatives considered**:
- **Serve frontend from Spring Boot (Thymeleaf or static resources)**: Eliminates a separate container but couples frontend and backend deployments — rejected (violates contract-driven decoupling principle).
- **Apache httpd**: Comparable feature set but larger image and more complex configuration than Nginx for this use case — rejected.
- **Node.js serve package**: Unnecessary Node.js runtime for files that need no processing — rejected.

---

## Decision 3: Database Container and Data Persistence

**Decision**: `postgres:15-alpine` with a named Docker volume (`postgres_data`) for data persistence across restarts.

**Rationale**: The official PostgreSQL 15 Alpine image is small (~240 MB) and production-aligned with the tech context defined in the 001 plan. A named volume (managed by Docker) persists data across `docker compose down` and `docker compose up` cycles, satisfying SC-004. Developers who need a clean slate can run `docker compose down -v`.

**Alternatives considered**:
- **Bind mount to host path**: Works but creates permission issues on Windows/macOS Docker Desktop — rejected.
- **No persistence (tmpfs)**: Data lost on restart — violates SC-004 — rejected.

---

## Decision 4: Health Checks and Service Start Order

**Decision**: The `db` service declares a `pg_isready` health check. The `backend` service uses `depends_on: db: condition: service_healthy`. No health check needed on `frontend` (static files are immediately available once Nginx starts).

**Rationale**: Spring Boot with Hibernate will throw a fatal connection error if it attempts to initialise the datasource before PostgreSQL is ready to accept connections. The `service_healthy` condition ensures Docker Compose waits for the health check to pass before starting the backend, satisfying FR-006 and the edge case identified in the spec.

**Alternatives considered**:
- **`depends_on` without health check**: Only waits for the container to start, not for PostgreSQL to be ready — rejected (race condition).
- **`wait-for-it.sh` script in backend entrypoint**: Works but adds complexity and a shell dependency to the runtime image — rejected in favour of native Compose health check support.

---

## Decision 5: Environment Variable and Configuration Strategy

**Decision**: All configurable values (credentials, ports, hostnames) are externalised to environment variables with safe development defaults inline in `docker-compose.yml`. An `.env.example` file documents all variables. Sensitive values (passwords) are in `.env` (git-ignored); `.env.example` contains placeholder values.

**Rationale**: Satisfies FR-008, FR-009, and SC-005. Using inline defaults in `docker-compose.yml` means the application runs out-of-the-box with `docker compose up` even if the developer skips creating `.env`, while still allowing overrides.

**Alternatives considered**:
- **Docker secrets**: Appropriate for Swarm/production but adds complexity for local development — rejected.
- **Hardcoded values in Compose file**: Violates SC-005 and makes it impossible to override without editing the file — rejected.

---

## Decision 6: Nginx API Proxy Configuration

**Decision**: Nginx proxies requests with path prefix `/api/` to `http://backend:8080`. The frontend JavaScript uses relative URLs (`/api/v1/...`) so no host/port is hardcoded in any JS file.

**Rationale**: Keeps frontend JS portable (works the same whether served by Nginx in Docker or by a local dev server with a proxy configured). Docker's internal DNS resolves `backend` to the backend container's IP automatically within the Compose network.

**Alternatives considered**:
- **Absolute backend URL in JS (e.g., `http://localhost:8080`)**: Hard to override per environment, breaks in Docker where the frontend container cannot reach `localhost:8080` of the backend — rejected.
- **CORS headers on backend, no proxy**: Would require CORS configuration in Spring Boot to allow the frontend origin, and the frontend would need the backend's external address — more moving parts — rejected for this context.
