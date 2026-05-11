# Quickstart: Running the Pet Shop App with Docker Compose

**Feature**: 002-docker-compose-setup  
**Date**: 2026-05-11

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (macOS/Windows) or Docker Engine + Compose plugin (Linux), version 24+.
- No Java, Maven, Node.js, or PostgreSQL installation required on the host.

## First-Time Setup

```bash
# 1. Clone the repository
git clone <repo-url>
cd spec-driven-dev-demo

# 2. Copy the environment template
cp .env.example .env
# Edit .env if you need custom ports or credentials (defaults work out of the box)

# 3. Build images and start all services
docker compose up --build
```

The `--build` flag is only needed the first time (or after source code changes). Subsequent starts use cached images.

## Daily Use

```bash
# Start all services (uses cached images)
docker compose up

# Start in background (detached)
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down
```

## Access the Application

| Service  | URL                        |
|----------|----------------------------|
| Frontend | http://localhost:80         |
| Backend API | http://localhost:8080/api/v1 |

> Ports are configurable via `.env` (`FRONTEND_PORT`, `BACKEND_PORT`).

## Running Specific Services

```bash
# Start only the database and backend (for API development)
docker compose up db backend

# Start only the frontend (assumes backend is already running)
docker compose up frontend
```

## Rebuilding After Source Changes

```bash
# Rebuild a specific service image
docker compose build backend
docker compose build frontend

# Rebuild all and restart
docker compose up --build
```

## Reset Everything (clean slate)

```bash
# Stop containers and remove the database volume (WARNING: deletes all data)
docker compose down -v
```

## Troubleshooting

**Port already in use**: Edit `FRONTEND_PORT` or `BACKEND_PORT` in `.env` and restart.

**Backend fails to connect to database**: The backend health-checks the database before starting. If it fails repeatedly, check that no other PostgreSQL instance is running on port 5432 and that the `POSTGRES_*` variables in `.env` match between the `db` and `backend` services.

**Schema mismatch after update**: If the database schema has changed, run `docker compose down -v` to drop the volume and restart with `docker compose up --build`.

**Slow first startup**: Docker pulls base images on first run. Subsequent starts are faster. The Spring Boot JVM also takes 5–10 seconds to initialise before the API is ready.
