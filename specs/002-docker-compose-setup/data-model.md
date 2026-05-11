# Data Model: Docker Compose Setup

**Feature**: 002-docker-compose-setup  
**Date**: 2026-05-11

## Note

This feature is infrastructure / developer tooling. It introduces no new domain entities and does not modify the application's data schema. The domain entities (Product, Category, User, Order, Cart, etc.) are defined in [001-pet-shop-webapp/data-model.md](../001-pet-shop-webapp/data-model.md).

## Service Dependency Model

The following describes the runtime dependency graph between Docker Compose services:

```
┌──────────┐        ┌─────────┐        ┌────────────┐
│ frontend │───────►│ backend │───────►│     db     │
│  (Nginx) │  proxy │(Spring) │  JDBC  │ (Postgres) │
└──────────┘        └─────────┘        └────────────┘
   port 80            port 8080          port 5432
  (host)             (internal)         (internal)
```

| Service    | Image                           | Exposes to Host | Depends On          |
|------------|-------------------------------|-----------------|---------------------|
| `db`       | `postgres:15-alpine`           | *(optional)*    | —                   |
| `backend`  | Built from `backend/Dockerfile` | `BACKEND_PORT` (default 8080) | `db` (healthy) |
| `frontend` | Built from `frontend/Dockerfile`| `FRONTEND_PORT` (default 80)  | `backend` (started) |

## Environment Variables

All variables have safe development defaults in `docker-compose.yml`. Override via `.env`.

| Variable              | Default Value          | Used By           | Description                        |
|-----------------------|------------------------|-------------------|------------------------------------|
| `POSTGRES_DB`         | `petshop`              | `db`              | Database name                      |
| `POSTGRES_USER`       | `petshop`              | `db`, `backend`   | Database username                  |
| `POSTGRES_PASSWORD`   | `petshop_secret`       | `db`, `backend`   | Database password                  |
| `BACKEND_PORT`        | `8080`                 | `backend`         | Host port mapped to backend        |
| `FRONTEND_PORT`       | `80`                   | `frontend`        | Host port mapped to frontend/Nginx |

## Named Volumes

| Volume Name     | Mounted In | Path                        | Purpose                  |
|-----------------|------------|-----------------------------|--------------------------|
| `postgres_data` | `db`       | `/var/lib/postgresql/data`  | Persistent database data |

## Docker Networks

Compose creates a default bridge network (`petshop_default`). All services join it automatically. The `frontend` container reaches the backend via `http://backend:8080` (Docker DNS); the `backend` reaches the database via `db:5432`.
