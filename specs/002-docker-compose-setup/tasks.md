# Tasks: Docker Compose Setup

**Input**: Design documents from `specs/002-docker-compose-setup/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Tests**: Not included — acceptance testing is manual via spec.md acceptance scenarios (no automated test framework for infrastructure at this scale).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no blocking dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths included in all task descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Environment configuration files required by all services before any Compose service is defined.

- [X] T001 Add `.env` to `.gitignore` at repository root (create `.gitignore` if it does not exist; append `.env` if it does)
- [X] T002 Create `.env.example` at repository root documenting all required variables with example values: `POSTGRES_DB=petshop`, `POSTGRES_USER=petshop`, `POSTGRES_PASSWORD=petshop_secret`, `BACKEND_PORT=8080`, `FRONTEND_PORT=80`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create the `docker-compose.yml` skeleton with the `db` service — the root dependency that all user stories and health-check chains depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T003 Create `docker-compose.yml` at repository root with the `db` service: image `postgres:15-alpine`, environment variables `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` with inline defaults (`petshop`, `petshop`, `petshop_secret`), health check `test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-petshop}"]` with `interval: 5s`, `timeout: 5s`, `retries: 5`, and volume mount `postgres_data:/var/lib/postgresql/data`
- [X] T004 Add top-level `volumes:` section declaring `postgres_data:` to `docker-compose.yml` (appended after the `services:` block)

**Checkpoint**: Foundation ready — `db` service is defined and health-checked. User story implementation can now begin.

---

## Phase 3: User Story 1 - Run the Full App with One Command (Priority: P1) 🎯 MVP

**Goal**: A developer with only Docker installed runs `docker compose up --build` and all three services (db, backend, frontend) start successfully and are accessible without any local Java, Maven, or Node.js installation.

**Independent Test**: Run `docker compose up --build` on a clean machine (Docker only). Navigate to `http://localhost:80`; verify the pet shop home page loads and displays products. Verify `http://localhost:8080/api/v1/categories` returns HTTP 200. Run `docker compose down` and confirm clean shutdown.

- [X] T005 [P] [US1] Create `backend/Dockerfile` with two named stages: (1) `builder` stage using `eclipse-temurin:21-jdk-alpine`, sets `WORKDIR /build`, copies `pom.xml` then `src/` to leverage layer caching, runs `./mvnw package -DskipTests` (or `mvn package -DskipTests` if no wrapper); (2) `runtime` stage using `eclipse-temurin:21-jre-alpine`, creates non-root group and user `appuser` with UID 1001 (`addgroup -S appuser && adduser -S appuser -G appuser`), sets `WORKDIR /app`, copies fat JAR from `builder` stage to `/app/app.jar`, sets ownership to `appuser`, switches to `appuser` with `USER appuser`, sets `ENTRYPOINT ["java", "-jar", "/app/app.jar"]`
- [X] T006 [P] [US1] Create `frontend/nginx.conf` with: a single `server` block listening on port 80, `root /usr/share/nginx/html`, `index index.html`, `location /` with `try_files $uri $uri/ /index.html`, `location /api/` with `proxy_pass http://backend:8080/api/`, `proxy_set_header Host $host`, `proxy_set_header X-Real-IP $remote_addr`, `proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for`, `proxy_set_header X-Forwarded-Proto $scheme`, `proxy_read_timeout 60s`, `proxy_connect_timeout 10s`
- [X] T007 [US1] Create `frontend/Dockerfile` using `nginx:1.25-alpine` as base, `COPY` all static files from `frontend/` (HTML, CSS, JS) to `/usr/share/nginx/html`, `COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf`, create non-root user `nginxuser` with UID 1001 (`addgroup -S nginxuser && adduser -S nginxuser -G nginxuser`), set appropriate ownership on html dir and nginx pid dir, switch to `USER nginxuser`, expose port 80, default `CMD ["nginx", "-g", "daemon off;"]`
- [X] T008 [US1] Add `backend` service to `docker-compose.yml`: `build: {context: ./backend}`, `depends_on: {db: {condition: service_healthy}}`, `ports: ["${BACKEND_PORT:-8080}:8080"]`, environment variables `SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/${POSTGRES_DB:-petshop}`, `SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-petshop}`, `SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-petshop_secret}`
- [X] T009 [US1] Add `frontend` service to `docker-compose.yml`: `build: {context: ./frontend}`, `depends_on: [backend]`, `ports: ["${FRONTEND_PORT:-80}:80"]`
- [X] T010 [US1] Smoke test User Story 1: run `docker compose up --build`, wait for all three services to report `healthy`/`running`, verify `http://localhost:80` serves the pet shop home page, verify `http://localhost:8080/api/v1/categories` returns HTTP 200 JSON, run `docker compose down` and confirm all containers stop cleanly

**Checkpoint**: User Story 1 complete — full application is startable with one command.

---

## Phase 4: User Story 2 - Backend and Database Run Together (Priority: P2)

**Goal**: A backend developer runs `docker compose up db backend` to get the API and database running without the frontend. The backend retries the database connection and only serves traffic once the database health check passes.

**Independent Test**: Run `docker compose up db backend`, wait for both services to be healthy. Send `GET http://localhost:8080/api/v1/categories` and verify HTTP 200 response with JSON data. Write a record via the API and verify it persists across a `docker compose down` / `docker compose up db backend` cycle.

- [X] T011 [US2] Confirm `depends_on: db: condition: service_healthy` is present in the `backend` service in `docker-compose.yml` (verify from T008; add the condition if it was added as a simple list rather than a map with condition key)
- [X] T012 [US2] Smoke test User Story 2: run `docker compose up db backend`, wait for both containers to report healthy, send `GET http://localhost:8080/api/v1/categories`, confirm HTTP 200 JSON response, POST a new resource, restart with `docker compose down && docker compose up db backend`, confirm the previously written data is still present (volume persistence)

**Checkpoint**: User Story 2 complete — backend and database run independently of the frontend.

---

## Phase 5: User Story 3 - Frontend Served via Nginx in Container (Priority: P3)

**Goal**: A developer runs the frontend container to preview the app exactly as it appears when served by Nginx, catching any path or proxy differences versus a local dev server.

**Independent Test**: Start full stack, navigate to `http://localhost:80`, open browser developer tools, verify all CSS and JS assets load with HTTP 200, trigger an API call (e.g., browse the product list), confirm the network tab shows `/api/v1/` requests going to port 80 (proxied through Nginx) with no CORS errors in the console.

- [X] T013 [US3] Review `frontend/nginx.conf` (from T006): confirm `proxy_pass` correctly strips the `/api/` prefix before forwarding to `http://backend:8080/api/` (trailing slash on both `location` and `proxy_pass` is required to avoid double `/api/api/` paths); adjust if incorrect
- [X] T014 [US3] Smoke test User Story 3: start `docker compose up --build`, open `http://localhost:80` in browser, open DevTools Network tab, verify `index.html` and all linked CSS/JS assets return HTTP 200, interact with the page to trigger an API call, confirm the XHR/fetch request is proxied through port 80 and returns data without any CORS error in the console

**Checkpoint**: User Story 3 complete — frontend Nginx container correctly serves static files and proxies API requests.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Security compliance, documentation validation, and final acceptance run.

- [X] T015 [P] Audit `docker-compose.yml`, `backend/Dockerfile`, and `frontend/Dockerfile` for any hardcoded credentials or hard-coded environment-specific values (SC-005); confirm all sensitive values are read from environment variables with safe defaults only
- [X] T016 [P] Verify `.env.example` at repository root contains every variable referenced in `docker-compose.yml` with a descriptive comment for each entry (FR-009 compliance)
- [X] T017 [P] Confirm both Dockerfiles switch to a non-root user before `CMD`/`ENTRYPOINT`: `appuser` in `backend/Dockerfile` and `nginxuser` in `frontend/Dockerfile` (FR-003 compliance)
- [X] T018 Run full clean-slate acceptance test: `docker compose down -v` to destroy all containers and volumes, then `docker compose up --build` to rebuild from scratch; verify all services start healthy within 60 seconds and the pet shop home page loads (SC-001, SC-002)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user story phases
- **User Story 1 (Phase 3)**: Depends on Phase 2 — creates ALL implementation files (Dockerfiles, nginx.conf, full compose)
- **User Story 2 (Phase 4)**: Depends on Phase 3 (validates a subset of US1 outputs)
- **User Story 3 (Phase 5)**: Depends on Phase 3 (validates the frontend subset of US1 outputs)
- **Polish (Phase 6)**: Depends on Phases 3, 4, 5

### Within Phase 3 (User Story 1)

- **T005** (backend/Dockerfile) and **T006** (frontend/nginx.conf) — parallel, different files
- **T007** (frontend/Dockerfile) — depends on T006 (copies nginx.conf)
- **T008** (add backend to compose) — depends on T003/T004 (foundational compose file)
- **T009** (add frontend to compose) — depends on T008 (sequential writes to same file)
- **T010** (smoke test) — depends on T005, T007, T009 (all files must exist)

### Parallel Opportunities

- T001 ‖ T002 (Phase 1 — different files)
- T005 ‖ T006 (Phase 3 — different files)
- T015 ‖ T016 ‖ T017 (Phase 6 — read-only audits)

---

## Parallel Example: User Story 1

```bash
# These two tasks can run simultaneously (different files):
Task T005: "Create backend/Dockerfile with multi-stage build"
Task T006: "Create frontend/nginx.conf with static serving and /api/ proxy"

# After T006 completes:
Task T007: "Create frontend/Dockerfile (depends on nginx.conf)"

# After T003/T004 (Foundational):
Task T008: "Add backend service to docker-compose.yml"

# After T008:
Task T009: "Add frontend service to docker-compose.yml"

# After T005, T007, T009:
Task T010: "Smoke test full stack — docker compose up --build"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T002)
2. Complete Phase 2: Foundational (T003–T004) — CRITICAL, blocks everything
3. Complete Phase 3: User Story 1 (T005–T010)
4. **STOP and VALIDATE**: `docker compose up --build`, confirm all three services healthy, frontend loads
5. Demo: full pet shop running with a single command

### Incremental Delivery

1. Setup + Foundational → db service defined with health check
2. User Story 1 → Full stack running → **Demo (MVP!)**
3. User Story 2 → Backend-only mode validated → backend devs unblocked
4. User Story 3 → Frontend Nginx validated → frontend preview confirmed
5. Polish → Security and documentation compliance

### Parallel Team Strategy

With two developers available after Phase 2:
- **Developer A**: User Story 1 — T005 ‖ T006, then T007, T008, T009, T010
- **Developer B**: `.env.example` review (T016) and security audit prep in parallel

---

## Notes

- [P] = task touches different files from its parallel sibling; no race conditions
- No automated tests — acceptance testing is manual via spec.md acceptance scenarios
- Smoke test tasks (T010, T012, T014, T018) must run sequentially (Docker Compose cannot be parallelized)
- Run `docker compose down -v` between smoke tests to avoid stale volume state
- Commit after each phase or logical group of tasks
- Stop at T010 checkpoint to validate MVP before continuing to US2/US3
