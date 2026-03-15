# Inst Bot (Single-Server Mode)

This project now runs frontend + backend as one server:
- React frontend is built during Docker build.
- Built frontend files are embedded into Spring Boot static resources.
- Spring Boot serves both UI and `/api/*`.

## Architecture

- One container: `inst-bot-backend`
- One public port: `80`
- Database: SQLite volume at `./data/inst_bot.db`

## Local Testing

### 1) Backend tests

```bash
./gradlew test
```

### 2) Frontend build test

```bash
cd frontend
npm ci
npm run build
```

### 3) Run as one server without Docker (port 8080)

```bash
cd /path/to/inst_bot
./gradlew bootRun
```

`bootRun` now triggers frontend build automatically.

Open:
- UI: `http://localhost:8080/`
- API example: `http://localhost:8080/api/dashboard/stats`

### 4) Full one-server run with Docker

```bash
cp .env.example .env
docker compose -f docker-compose.arm.yml up --build
```

Open:
- UI: `http://localhost:18081/` (or your `HOST_HTTP_PORT`)
- API example: `http://localhost:18081/api/dashboard/stats`

## Hosting on ARM server (GitHub Actions)

Workflow: `.github/workflows/deploy-dev-arm.yml`

Trigger:
- push to branch `dev_backend`
- or manual run (`workflow_dispatch`)

Required GitHub secrets:
- `DEV_HOST`
- `DEV_USER`
- `DEV_SSH_PRIVATE_KEY`
- `DEV_DEPLOY_PATH`

What deploy does:
1. Uploads project archive to ARM server.
2. Preserves `.env` and `data/`.
3. Fixes write permissions on `data/` for SQLite.
4. Builds backend image natively on ARM (no QEMU emulation).
5. Starts/updates service via `docker compose -f docker-compose.arm.yml up -d`.

## Environment Variables

Use `.env` on server (example in `.env.example`):
- `HOST_HTTP_PORT` (default `18081`, set any free port)
- `IG_USERNAME`
- `IG_PASSWORD`
- `RAPIDAPI_KEY`
- `APIFY_TOKEN`
- `GEMINI_API_KEY`
- `OLLAMA_BASE_URL`
- `OLLAMA_MODEL`
