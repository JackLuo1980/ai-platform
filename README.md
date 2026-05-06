# AI/MLOps Platform

Enterprise AI/MLOps platform for model lifecycle management, featuring model training, inference serving, data labeling, and scoring services.

## Architecture

```
                          ┌─────────────┐
                          │   Frontend  │
                          │ React + Ant │
                          └──────┬──────┘
                                 │
                          ┌──────▼──────┐
                          │   Gateway   │
                          │ Spring Cloud│
                          └──────┬──────┘
                                 │
          ┌──────────┬───────────┼───────────┬──────────┐
          │          │           │           │          │
     ┌────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌───▼────┐ ┌───▼────┐
     │Console │ │Operation│ │  Lab   │ │Inference│ │FastLabel│
     │ Admin  │ │  Ops    │ │Train   │ │ Serve  │ │  Data  │
     └────┬───┘ └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
          │         │          │          │          │
          └─────────┴──────────┼──────────┴──────────┘
                             │
                    ┌────────▼────────┐
                    │    Scorecard    │
                    │  Scoring Engine │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
    ┌─────▼─────┐    ┌──────▼──────┐    ┌─────▼─────┐
    │ PostgreSQL │    │    Redis    │    │   MinIO   │
    │   + Flyway │    │   Cache    │    │  Storage  │
    └───────────┘    └─────────────┘    └───────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| gateway | 8080 | API gateway, routing, rate limiting |
| console | 8081 | Admin console, user/role management |
| operation | 8082 | DevOps, model registry, pipeline management |
| lab | 8083 | Experiment tracking, model training |
| inference | 8084 | Model serving, batch prediction |
| fastlabel | 8085 | Data labeling, annotation management |
| scorecard | 8086 | Scoring engine, model evaluation |

## Shared Libraries

| Library | Description |
|---------|-------------|
| common-model | JPA entities, DTOs, enums, domain events |
| common-security | JWT authentication, RBAC, audit logging |
| common-storage | MinIO object storage wrapper |
| common-testing | Testcontainers, mock utilities |

## Build

```bash
./gradlew build
```

## Tech Stack

- Java 21, Spring Boot 3.3.6, Spring Cloud 2024.0.0
- MyBatis-Plus 3.5.9, Flyway, PostgreSQL
- React 18, Ant Design 5, Vite
