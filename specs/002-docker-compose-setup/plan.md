# Implementation Plan: Docker Compose Setup

**Branch**: `002-docker-compose-setup` | **Date**: 2026-05-11 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `specs/002-docker-compose-setup/spec.md`

## Summary

Add Docker Compose orchestration so the entire pet shop application — Spring Boot backend, PostgreSQL database, and Nginx-served static frontend — can be started with `docker compose up` on any machine with Docker installed. Three files are added to the repository root and two Dockerfiles are added to the existing `backend/` and `frontend/` directories. No application source code changes are required.

## Technical Context

**Language/Version**: YAML (Compose file); Dockerfile DSL; Nginx 1.25; shell (health check commands)  
**Primary Dependencies**: Docker Engine 24+ / Compose plugin v2.20+; `postgres:15-alpine`; `eclipse-temurin:21-jdk-alpine` (build) + `eclipse-temurin:21-jre-alpine` (runtime); `nginx:1.25-alpine`  
**Storage**: Named Docker volume `postgres_data` for PostgreSQL data persistence  
**Testing**: Manual smoke test per acceptance scenario (no automated test framework for infrastructure at this scale)  
**Target Platform**: Any OS with Docker installed (Linux, macOS, Windows via Docker Desktop)  
**Project Type**: Container orchestration / developer tooling  
**Performance Goals**: All three services healthy within 60 seconds of `docker compose up --build` on a machine with images already pulled  
**Constraints**: No root user in any container (FR-003); no hardcoded secrets in Dockerfiles or Compose file (SC-005); local development only — no production hardening in scope  
**Scale/Scope**: 3 services; 2 Dockerfiles; 1 Compose file; 1 Nginx config; 1 `.env.example`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Spec-First Development | ✅ PASS | `specs/002-docker-compose-setup/spec.md` exists and is complete |
| II. Contract-Driven Design | ✅ PASS (adapted) | No external API introduced. The Compose service names, port bindings, and environment variable names form the internal "contract" between services. The Nginx proxy keeps the frontend–backend interface unchanged from the established `/api/v1` contract. |
| III. Test-First (NON-NEGOTIABLE) | ✅ PASS (adapted) | Infrastructure has no unit-testable logic. Acceptance scenarios in the spec serve as the test plan; each scenario is verified manually after implementation. |
| IV. E2E Testability & Accessibility | ✅ PASS | Each user story (full stack, backend+db only, frontend only) is independently executable and verifiable with `docker compose up <services>`. No UI introduced — accessibility is N/A. |
| V. Simplicity & Incremental Delivery | ✅ PASS | Minimum viable set of files: 2 Dockerfiles + 1 Compose file + 1 Nginx config + 2 env files. No Kubernetes, no CI pipeline, no multi-stage Compose overrides beyond what is needed. |

**Post-Phase 1 re-check**: All principles pass. The service dependency model (data-model.md) is minimal and maps directly to spec requirements. No added complexity.

## Project Structure

### Documentation (this feature)

```text
specs/002-docker-compose-setup/
├── plan.md          # This file
├── research.md      # Phase 0 output
├── data-model.md    # Phase 1 output (service dependency model)
├── quickstart.md    # Phase 1 output
└── tasks.md         # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

*(No `contracts/` directory: this feature exposes no new external interfaces.)*

### Source Code (repository root)

```text
backend/
└── Dockerfile            # Multi-stage: JDK+Maven build → JRE runtime

frontend/
├── Dockerfile            # Nginx static file server
└── nginx.conf            # Nginx server config with /api/ proxy

docker-compose.yml        # Orchestrates db, backend, frontend
.env.example              # Documents all environment variables
.env                      # Local overrides — git-ignored
```

**Structure Decision**: Files are placed at the conventional locations expected by Docker Compose. `docker-compose.yml` at the repository root is the standard entry point; service Dockerfiles live inside their respective source directories (`backend/`, `frontend/`) so `docker compose build` uses the directory as build context with access to `pom.xml`, source files, and static HTML/CSS/JS.

## Complexity Tracking

> No constitution violations. Table left intentionally empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| — | — | — |
