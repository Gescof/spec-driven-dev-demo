<!-- SYNC IMPACT REPORT
Version change: N/A → 1.0.0 (initial creation)
Modified principles: N/A (new document)
Added sections: Core Principles, Development Standards, Development Workflow, Governance
Removed sections: N/A
Templates requiring updates:
  ✅ .specify/templates/plan-template.md — Constitution Check section is template-agnostic; no update needed
  ✅ .specify/templates/spec-template.md — User story structure aligns with Spec-First and independent testability; no update needed
  ✅ .specify/templates/tasks-template.md — Phase/priority structure aligns with incremental delivery and TDD; no update needed
  ✅ .specify/templates/commands/ — No command files found; skipped
Follow-up TODOs:
  - RATIFICATION_DATE set to initial creation date (2026-05-11); update if a different formal adoption date is preferred
-->

# Spec-Driven Dev Demo Constitution

## Core Principles

### I. Spec-First Development

Every feature MUST have an approved specification before any implementation begins.
No production code is written without a corresponding `spec.md` entry.
Features are expressed as user stories with explicit acceptance scenarios and priorities.
The specification is the single source of truth; implementation follows the spec, not the
other way around.

**Rationale**: Prevents scope creep and misaligned delivery by anchoring all work to
explicit, user-approved requirements. Enables async collaboration and clear review criteria.

### II. Contract-Driven Design

Frontend and backend are decoupled through explicit, versioned API contracts.
All API contracts MUST be specified in `contracts/` before implementation begins.
Breaking contract changes MUST increment the API major version and MUST have a migration
path documented in `plan.md`.
Frontend components MUST NOT make assumptions about API shape beyond the published contract.

**Rationale**: Enables parallel frontend/backend development and surfaces integration
failures before they reach production. Makes breaking changes deliberate and tracked.

### III. Test-First (NON-NEGOTIABLE)

Tests MUST be written before implementation and MUST fail (Red) before any implementation
begins.
The Red-Green-Refactor cycle is strictly enforced for all new functionality.
Unit tests, contract tests, and integration tests are required for all P1 user stories.
No pull request introducing new functionality is merged without passing tests.

**Rationale**: Test-first design surfaces API ergonomics issues early, prevents regression,
and ensures the spec is actually verifiable rather than aspirational.

### IV. End-to-End Testability & Accessibility

Each user story MUST be independently testable end-to-end after its implementation phase
completes.
P1 user stories MUST include end-to-end (E2E) test coverage.
All frontend UI MUST meet WCAG 2.1 AA accessibility standards before a story is closed.
Each story delivers a self-contained, deployable increment of value — demo-able on its own.

**Rationale**: Independent testability prevents integration debt and enables incremental
delivery. Accessibility is a non-negotiable quality standard, not an afterthought.

### V. Simplicity & Incremental Delivery

Features are implemented in user story priority order (P1 → P2 → P3).
YAGNI applies strictly: no feature or abstraction is built without a specification backing
it.
Three similar lines of code are preferable to a premature abstraction.
Complexity MUST be justified in the `plan.md` Complexity Tracking table with an explicit
rationale for why simpler alternatives were rejected.

**Rationale**: Keeps the codebase maintainable and delivery predictable. Abstractions
introduced without proven need become tech debt faster than they add value.

## Development Standards

- **Technology**: Full-stack web application; frontend and backend may use any framework
  aligned with the project's `plan.md` Technical Context section.
- **Code Quality**: All code MUST pass linting and formatting checks configured in
  `plan.md` before merge.
- **Accessibility**: Frontend MUST be audited against WCAG 2.1 AA before each user story
  is marked complete.
- **Security**: Input validation is required at all system boundaries (user input, external
  APIs). Server-side code MUST NOT trust client-supplied data without validation.
- **Observability**: Structured logging MUST be present for all error paths and key
  business events. Log format MUST be machine-parseable (JSON preferred).

## Development Workflow

- All features begin with `/speckit-specify` and MUST NOT proceed to planning without an
  approved `spec.md`.
- Planning (`/speckit-plan`) produces `plan.md` with a Constitution Check section that
  MUST pass before implementation starts.
- Task generation (`/speckit-tasks`) produces a `tasks.md` organized by user story
  priority; tasks MUST follow the phase structure defined in the tasks template.
- Pull requests MUST reference the originating spec and MUST include passing tests for all
  implemented user stories.
- Code review MUST verify constitution compliance. A PR that violates a principle without
  documented justification in the Complexity Tracking table MUST be rejected.

## Governance

This constitution supersedes all other project practices and conventions. When the
constitution conflicts with a team habit, the constitution wins — or the constitution is
formally amended.

**Amendment procedure**:
1. Open a spec describing the proposed change and its rationale.
2. Review and approve the amendment with the team.
3. Update this file, increment the version following the versioning policy, and update
   `LAST_AMENDED_DATE`.
4. Update any affected templates and document a migration path for in-flight work.

**Versioning policy**:
- MAJOR: A principle is removed or fundamentally redefined (backward-incompatible
  governance change).
- MINOR: A new principle or section is added, or guidance is materially expanded.
- PATCH: Clarifications, wording fixes, or non-semantic refinements.

**Compliance review**: All PRs and plan reviews MUST include a Constitution Check step.
Violations MUST be documented in `plan.md` Complexity Tracking with justification.
Runtime development guidance lives in `CLAUDE.md`.

**Version**: 1.0.0 | **Ratified**: 2026-05-11 | **Last Amended**: 2026-05-11
