# Spec-Driven Dev Demo

A working demonstration of **spec-driven development** — a methodology where every feature begins with an approved specification and flows through a structured workflow before a single line of production code is written.

The application built by this demo is a **Pet Shop e-commerce web app**: Spring Boot REST API backend, vanilla HTML/CSS/JS frontend, PostgreSQL database, and Docker Compose orchestration.

---

## What is spec-driven development?

Each feature follows a strict, gated workflow:

```
/speckit-specify  →  /speckit-plan  →  /speckit-tasks  →  /speckit-implement
```

| Step | Output | Purpose |
|------|--------|---------|
| `speckit-specify` | `spec.md` | User stories, acceptance scenarios, requirements, success criteria |
| `speckit-plan` | `plan.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/` | Design, architecture, API contracts |
| `speckit-tasks` | `tasks.md` | Dependency-ordered implementation tasks |
| `speckit-implement` | Source code | Executes tasks, respects constitution gates |

No implementation begins until the spec passes a **Constitution Check** (see below).

---

## Project Constitution

Five non-negotiable principles govern all development:

1. **Spec-First** — no production code without an approved `spec.md`
2. **Contract-Driven** — frontend/backend decoupled through explicit, versioned API contracts
3. **Test-First** — Red-Green-Refactor strictly enforced; tests written before implementation
4. **End-to-End Testability** — each user story is independently testable after its implementation phase
5. **Simplicity & Incremental Delivery** — YAGNI; P1 → P2 → P3 story order; complexity must be justified

Full constitution: [.specify/memory/constitution.md](.specify/memory/constitution.md)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA |
| Frontend | Vanilla HTML/CSS/JavaScript (ES2022) |
| Database | PostgreSQL 15 (production), H2 in-memory (test profile) |
| Infrastructure | Docker Compose |
| Testing | JUnit 5, MockMvc, Spring Security Test |
| Build | Maven 3.9+ |

---

## Features (branches)

| Branch | Feature | Status |
|--------|---------|--------|
| `001-pet-shop-webapp` | Core web app — catalog, accounts, cart, checkout | Implemented |
| `002-docker-compose-setup` | Docker Compose for full-stack local dev | Implemented |
| `003-dto-records-yaml-config` | Convert DTOs to Java records; migrate config to YAML | In progress |

Each branch's spec and plan live under `specs/<branch-name>/`.

---

## Running the app

```bash
# Full stack (requires Docker)
docker compose up --build

# Backend + DB only
docker compose up db backend
```

See [specs/002-docker-compose-setup/quickstart.md](specs/002-docker-compose-setup/quickstart.md) for detailed setup instructions.

---

## Repository structure

```text
.
├── backend/                  # Spring Boot REST API
├── frontend/                 # Static HTML/CSS/JS frontend
├── specs/
│   ├── 001-pet-shop-webapp/  # spec.md, plan.md, tasks.md, contracts/, …
│   ├── 002-docker-compose-setup/
│   └── 003-dto-records-yaml-config/
├── .specify/
│   ├── memory/constitution.md
│   └── templates/            # spec, plan, tasks, checklist templates
├── docker-compose.yml
└── CLAUDE.md
```

---

## Spec Kit commands

These Claude Code slash commands drive the workflow:

| Command | What it does |
|---------|-------------|
| `/speckit-specify` | Create or update a feature spec from a natural-language description |
| `/speckit-clarify` | Ask targeted questions to resolve underspecified areas in the spec |
| `/speckit-plan` | Generate design artifacts: plan, research, data model, quickstart, API contracts |
| `/speckit-tasks` | Generate a dependency-ordered `tasks.md` |
| `/speckit-implement` | Execute all tasks in `tasks.md` |
| `/speckit-analyze` | Cross-artifact consistency and quality check |
| `/speckit-checklist` | Generate a custom checklist for the current feature |
| `/speckit-constitution` | Create or update the project constitution |

---

## License

[MIT](LICENSE)
