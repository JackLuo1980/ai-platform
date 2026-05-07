# AI Platform (APS Clone)

Enterprise MLOps platform for full model lifecycle management — data labeling, experiment tracking, workflow orchestration, model serving, drift monitoring, and credit scorecard scoring.

## Architecture

```
  ┌─────────────────────────────────────────────────────────────────────┐
  │                        ai-29 (91.233.10.29)                        │
  │  ┌──────────┐  ┌───────┐  ┌───────┐  ┌───────┐  ┌──────────────┐  │
  │  │  Nginx   │  │Redis 7│  │NATS   │  │Frontend│  │  WireGuard   │  │
  │  │ :80/:443 │  │ :6379 │  │ :4222 │  │ :3000  │  │   Hub        │  │
  │  └────┬─────┘  └───────┘  └───────┘  └───────┘  │  10.0.0.1    │  │
  │       │                                         └──────┬───────┘  │
  └───────┼────────────────────────────────────────────────┼──────────┘
          │ proxy /api/*                          WG tunnel│
          ▼                                                 │
  ┌────────────────────────────────────────────────────────┼──────────┐
  │              ai-v6a (10.0.0.2)                         │          │
  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐│          │
  │  │ Gateway  │  │ Console  │  │Operation │  │MinIO   ││          │
  │  │ :8080    │  │ :8081    │  │ :8082    │  │ :9000  ││          │
  │  └──────────┘  └──────────┘  └──────────┘  └────────┘│          │
  │  ┌──────────────────────┐                              │          │
  │  │    PostgreSQL 16     │                              │          │
  │  │    :5432             │                              │          │
  │  └──────────────────────┘                              │          │
  └────────────────────────────────────────────────────────┼──────────┘
                                                           │
  ┌────────────────────────────────────────────────────────┼──────────┐
  │              ai-v6b (10.0.0.3)                         │          │
  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐│          │
  │  │   Lab    │  │Inference │  │FastLabel │  │Scorecard││          │
  │  │ :8083    │  │ :8084    │  │ :8085    │  │ :8086  ││          │
  │  └──────────┘  └──────────┘  └──────────┘  └────────┘│          │
  │  ┌──────────────────────┐                              │          │
  │  │     MLflow Server    │                              │          │
  │  │     :5000            │                              │          │
  │  └──────────────────────┘                              │          │
  └───────────────────────────────────────────────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.3.6, Spring Cloud Gateway, MyBatis-Plus 3.5.9 |
| Frontend | React 18, Ant Design 5, Vite, TypeScript |
| Database | PostgreSQL 16, Flyway migrations |
| Cache/MQ | Redis 7, NATS |
| Storage | MinIO (S3-compatible) |
| MLOps | MLflow (experiment tracking, model registry) |
| Infra | Docker, Docker Compose, Nginx, WireGuard VPN |

## Module Overview

| Service | Port | Module | Description |
|---------|------|--------|-------------|
| gateway | 8080 | `services/gateway` | API gateway with JWT auth, rate limiting, routing |
| console | 8081 | `services/console` | User/role/tenant/project management, audit logs |
| operation | 8082 | `services/operation` | Cluster, image, environment, resource pool/quota management |
| lab | 8083 | `services/lab` | Data sources, datasets, experiments, workflows, feature store, model archive |
| inference | 8084 | `services/inference` | Model import, online/batch serving, evaluation, drift detection, monitoring |
| fastlabel | 8085 | `services/fastlabel` | Data labeling tasks, datasets, items, annotation, export |
| scorecard | 8086 | `services/scorecard` | Credit scoring: variables, binning, WOE/IV, model training, scoring rules |
| frontend | 3000 | `frontend` | React SPA with Ant Design |

### Shared Libraries

| Library | Description |
|---------|-------------|
| `shared/common-model` | JPA entities, DTOs, enums, domain events, `R<T>` response wrapper |
| `shared/common-security` | JWT authentication, RBAC, audit logging |
| `shared/common-storage` | MinIO object storage wrapper |
| `shared/common-testing` | Testcontainers, mock utilities |

## Quick Start

### Prerequisites

- JDK 21+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 16, Redis 7, MinIO, NATS

### Build

```bash
# Build all backend services
./gradlew build -x test

# Build frontend
cd frontend && npm install && npm run build && cd ..
```

### Local Development

```bash
# Start infrastructure (PostgreSQL, Redis, MinIO, NATS)
cd infra/docker && docker compose up -d postgresql redis minio nats

# Start services (each in a separate terminal)
./gradlew :services:gateway:bootRun
./gradlew :services:console:bootRun
# ... or run Ai*Application main class from IDE

# Start frontend dev server
cd frontend && npm run dev
```

### Production Deploy

```bash
# Full deployment via script
./scripts/deploy-core.sh

# Or see the detailed guide
# → docs/deployment-guide.md
```

## API Access

| Environment | URL |
|-------------|-----|
| Production | `https://<your-domain>/api/` |
| Local dev | `http://localhost:8080/api/` |

### Default Credentials

| Service | Username | Password |
|---------|----------|----------|
| Admin login | `admin` | `admin123` |
| PostgreSQL | `aiplatform` | `AiPlatform2026Pg!` |
| MinIO | `aiplatform` | `AiPlatform2026Minio!` |

### Authentication

All API requests require a JWT token in the `Authorization: Bearer <token>` header (except `/auth/login`).

```bash
# Login
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

## Documentation

| Document | Description |
|----------|-------------|
| [Deployment Guide](docs/deployment-guide.md) | Full production deployment instructions |
| [Operations Manual](docs/operations.md) | Service management, monitoring, troubleshooting |
| [API Reference](docs/api-reference.md) | Complete REST API documentation |

## Project Structure

```
ai-platform/
├── services/                 # Microservices
│   ├── gateway/             # API gateway
│   ├── console/             # Admin console
│   ├── operation/           # DevOps management
│   ├── lab/                 # ML lab & training
│   ├── inference/           # Model serving
│   ├── fastlabel/           # Data labeling
│   └── scorecard/           # Credit scoring
├── shared/                  # Shared libraries
│   ├── common-model/
│   ├── common-security/
│   ├── common-storage/
│   └── common-testing/
├── frontend/                # React frontend
├── infra/
│   ├── docker/              # Docker Compose, Nginx config, Dockerfiles
│   └── k8s/                 # Kubernetes manifests
├── scripts/                 # Deployment and seed scripts
├── build.gradle.kts
└── settings.gradle.kts
```

## License

Proprietary — internal use only.
