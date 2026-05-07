# Deployment Guide

Complete guide for deploying AI Platform to a 3-server production environment.

## Server Architecture

| Hostname | IP | WireGuard | Role |
|----------|----|-----------|------|
| ai-29 | 91.233.10.29 | 10.0.0.1 (Hub) | WireGuard Hub, Nginx reverse proxy, Redis, NATS, Frontend |
| ai-v6a | (via WG) | 10.0.0.2 | PostgreSQL, MinIO, Gateway, Console, Operation |
| ai-v6b | (via WG) | 10.0.0.3 | Lab, Inference, FastLabel, Scorecard, MLflow |

### Network Flow

```
Internet → 91.233.10.29 (Nginx:80/443)
  ├── /           → Frontend static files
  ├── /api/v1/*   → Gateway (10.0.0.2:8080)
  │                  ├── /auth/**          → Console   (10.0.0.2:8081)
  │                  ├── /users/**         → Console   (10.0.0.2:8081)
  │                  ├── /clusters/**      → Operation (10.0.0.2:8082)
  │                  ├── /lab/**           → Lab       (10.0.0.3:8083)
  │                  ├── /inference/**     → Inference (10.0.0.3:8084)
  │                  ├── /fastlabel/**     → FastLabel (10.0.0.3:8085)
  │                  └── /scorecard/**     → Scorecard (10.0.0.3:8086)
  └── /auth/*    → Gateway (same as /api)
```

## Prerequisites

### All Servers

- Ubuntu 22.04+ (or equivalent Linux)
- JDK 21 (Temurin recommended)
- Docker 24+ and Docker Compose v2+
- `curl`, `wget`, `unzip`
- SSH key-based authentication
- Minimum 4 GB RAM, 2 CPU cores

### ai-29 Specific

- Public IP reachable from the internet
- Domain DNS A record pointing to 91.233.10.29
- Ports 80, 443 open in firewall

### Install JDK 21

```bash
sudo apt update && sudo apt install -y openjdk-21-jdk-headless
java -version
```

### Install Docker

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
docker --version && docker compose version
```

## WireGuard VPN Setup

WireGuard connects all 3 servers into a private mesh network. ai-29 acts as the hub.

### ai-29 (Hub) — 10.0.0.1

```bash
sudo apt install -y wireguard
wg genkey | tee /etc/wireguard/server.key | wg pubkey > /etc/wireguard/server.pub
```

Create `/etc/wireguard/wg0.conf` on ai-29:

```ini
[Interface]
PrivateKey = <ai-29-private-key>
Address = 10.0.0.1/24
ListenPort = 51820

[Peer]
# ai-v6a
PublicKey = <ai-v6a-public-key>
AllowedIPs = 10.0.0.2/32

[Peer]
# ai-v6b
PublicKey = <ai-v6b-public-key>
AllowedIPs = 10.0.0.3/32
```

### ai-v6a — 10.0.0.2

```bash
sudo apt install -y wireguard
wg genkey | tee /etc/wireguard/server.key | wg pubkey > /etc/wireguard/server.pub
```

Create `/etc/wireguard/wg0.conf` on ai-v6a:

```ini
[Interface]
PrivateKey = <ai-v6a-private-key>
Address = 10.0.0.2/24

[Peer]
# ai-29 hub
PublicKey = <ai-29-public-key>
Endpoint = 91.233.10.29:51820
AllowedIPs = 10.0.0.0/24
PersistentKeepalive = 25
```

### ai-v6b — 10.0.0.3

Create `/etc/wireguard/wg0.conf` on ai-v6b:

```ini
[Interface]
PrivateKey = <ai-v6b-private-key>
Address = 10.0.0.3/24

[Peer]
# ai-29 hub
PublicKey = <ai-29-public-key>
Endpoint = 91.233.10.29:51820
AllowedIPs = 10.0.0.0/24
PersistentKeepalive = 25
```

### Enable WireGuard on All Servers

```bash
sudo systemctl enable wg-quick@wg0
sudo systemctl start wg-quick@wg0

# Verify
sudo wg show wg0
ping 10.0.0.2  # from ai-29
ping 10.0.0.3  # from ai-29
```

## Deployment Steps

### Step 1: Clone Repository

```bash
# On your build machine (or ai-29)
git clone <repo-url> /opt/ai-platform
cd /opt/ai-platform
```

### Step 2: Build All Services

```bash
cd /opt/ai-platform
./gradlew build -x test
```

Output JARs are at:
- `services/gateway/build/libs/gateway.jar`
- `services/console/build/libs/console.jar`
- `services/operation/build/libs/operation.jar`
- `services/lab/build/libs/lab.jar`
- `services/inference/build/libs/inference.jar`
- `services/fastlabel/build/libs/fastlabel.jar`
- `services/scorecard/build/libs/scorecard.jar`

### Step 3: Build Frontend

```bash
cd frontend
npm install
npm run build
# Output: frontend/dist/
```

### Step 4: Distribute JARs to Servers

```bash
# Copy to ai-v6a (gateway, console, operation)
scp services/gateway/build/libs/gateway.jar root@10.0.0.2:/opt/ai-platform/
scp services/console/build/libs/console.jar root@10.0.0.2:/opt/ai-platform/
scp services/operation/build/libs/operation.jar root@10.0.0.2:/opt/ai-platform/

# Copy to ai-v6b (lab, inference, fastlabel, scorecard)
scp services/lab/build/libs/lab.jar root@10.0.0.3:/opt/ai-platform/
scp services/inference/build/libs/inference.jar root@10.0.0.3:/opt/ai-platform/
scp services/fastlabel/build/libs/fastlabel.jar root@10.0.0.3:/opt/ai-platform/
scp services/scorecard/build/libs/scorecard.jar root@10.0.0.3:/opt/ai-platform/

# Copy frontend to ai-29
scp -r frontend/dist/* root@91.233.10.29:/opt/ai-platform/frontend/dist/
```

### Step 5: Start Infrastructure

On **ai-29** — Redis and NATS:

```bash
docker run -d --name redis --restart unless-stopped \
  -p 6379:6379 \
  redis:7-alpine

docker run -d --name nats --restart unless-stopped \
  -p 4222:4222 -p 8222:8222 \
  nats:latest
```

On **ai-v6a** — PostgreSQL and MinIO:

```bash
docker run -d --name postgresql --restart unless-stopped \
  -p 5432:5432 \
  -e POSTGRES_DB=aiplatform \
  -e POSTGRES_USER=aiplatform \
  -e POSTGRES_PASSWORD=AiPlatform2026Pg! \
  -v /opt/ai-platform/pgdata:/var/lib/postgresql/data \
  postgres:16-alpine

docker run -d --name minio --restart unless-stopped \
  -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=aiplatform \
  -e MINIO_ROOT_PASSWORD=AiPlatform2026Minio! \
  -v /opt/ai-platform/minio-data:/data \
  minio/minio server /data --console-address ":9001"
```

### Step 6: Run Flyway Migrations

```bash
# From ai-v6a (or any machine with DB access)
docker run --rm --network host \
  -e FLYWAY_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform \
  -e FLYWAY_USER=aiplatform \
  -e FLYWAY_PASSWORD='AiPlatform2026Pg!' \
  -v /opt/ai-platform/services/console/src/main/resources/db/migration:/flyway/sql \
  flyway/flyway:11 migrate
```

Or run migrations as part of the Console service startup (Flyway is enabled in `application-prod.yml`).

### Step 7: Start Services

#### ai-v6a — Gateway, Console, Operation

Create `/opt/ai-platform/docker-compose.yml` on ai-v6a:

```yaml
services:
  gateway:
    image: eclipse-temurin:21-jre-alpine
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=aiplatform
      - SPRING_DATASOURCE_PASSWORD=AiPlatform2026Pg!
      - SPRING_DATA_REDIS_HOST=10.0.0.1
      - SPRING_DATA_REDIS_PORT=6379
      - NATS_URL=nats://10.0.0.1:4222
    volumes:
      - ./gateway.jar:/app/app.jar:ro
    mem_limit: 1g
    restart: unless-stopped
    command: ["java", "-jar", "/app/app.jar"]

  console:
    image: eclipse-temurin:21-jre-alpine
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=aiplatform
      - SPRING_DATASOURCE_PASSWORD=AiPlatform2026Pg!
      - SPRING_DATA_REDIS_HOST=10.0.0.1
      - SPRING_DATA_REDIS_PORT=6379
      - NATS_URL=nats://10.0.0.1:4222
    volumes:
      - ./console.jar:/app/app.jar:ro
    mem_limit: 1g
    restart: unless-stopped
    command: ["java", "-jar", "/app/app.jar"]

  operation:
    image: eclipse-temurin:21-jre-alpine
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=aiplatform
      - SPRING_DATASOURCE_PASSWORD=AiPlatform2026Pg!
      - SPRING_DATA_REDIS_HOST=10.0.0.1
      - SPRING_DATA_REDIS_PORT=6379
      - NATS_URL=nats://10.0.0.1:4222
    volumes:
      - ./operation.jar:/app/app.jar:ro
    mem_limit: 1g
    restart: unless-stopped
    command: ["java", "-jar", "/app/app.jar"]
```

```bash
cd /opt/ai-platform && docker compose up -d
```

#### ai-v6b — Lab, Inference, FastLabel, Scorecard, MLflow

Create `/opt/ai-platform/docker-compose.yml` on ai-v6b:

```yaml
services:
  lab:
    image: eclipse-temurin:21-jre-alpine
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=aiplatform
      - SPRING_DATASOURCE_PASSWORD=AiPlatform2026Pg!
      - SPRING_DATA_REDIS_HOST=10.0.0.1
      - SPRING_DATA_REDIS_PORT=6379
      - NATS_URL=nats://10.0.0.1:4222
      - MLFLOW_TRACKING_URI=http://mlflow:5000
    volumes:
      - ./lab.jar:/app/app.jar:ro
    mem_limit: 2g
    restart: unless-stopped
    command: ["java", "-jar", "/app/app.jar"]

  inference:
    image: eclipse-temurin:21-jre-alpine
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=aiplatform
      - SPRING_DATASOURCE_PASSWORD=AiPlatform2026Pg!
      - SPRING_DATA_REDIS_HOST=10.0.0.1
      - SPRING_DATA_REDIS_PORT=6379
      - NATS_URL=nats://10.0.0.1:4222
    volumes:
      - ./inference.jar:/app/app.jar:ro
    mem_limit: 1536m
    restart: unless-stopped
    command: ["java", "-jar", "/app/app.jar"]

  fastlabel:
    image: eclipse-temurin:21-jre-alpine
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=aiplatform
      - SPRING_DATASOURCE_PASSWORD=AiPlatform2026Pg!
      - SPRING_DATA_REDIS_HOST=10.0.0.1
      - SPRING_DATA_REDIS_PORT=6379
      - NATS_URL=nats://10.0.0.1:4222
    volumes:
      - ./fastlabel.jar:/app/app.jar:ro
    mem_limit: 1536m
    restart: unless-stopped
    command: ["java", "-jar", "/app/app.jar"]

  scorecard:
    image: eclipse-temurin:21-jre-alpine
    ports:
      - "8086:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://10.0.0.2:5432/aiplatform?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=aiplatform
      - SPRING_DATASOURCE_PASSWORD=AiPlatform2026Pg!
    volumes:
      - ./scorecard.jar:/app/app.jar:ro
    mem_limit: 512m
    restart: unless-stopped
    command: ["java", "-jar", "/app/app.jar"]

  mlflow:
    image: python:3.10-slim
    ports:
      - "5000:5000"
    environment:
      - AWS_ACCESS_KEY_ID=aiplatform
      - AWS_SECRET_ACCESS_KEY=AiPlatform2026Minio!
      - AWS_ENDPOINT_URL=http://10.0.0.2:9000
      - MLFLOW_S3_ENDPOINT_URL=http://10.0.0.2:9000
    mem_limit: 2g
    restart: unless-stopped
    command: >
      sh -c "pip install mlflow psycopg2-binary boto3 -q &&
             mlflow server --host 0.0.0.0 --port 5000
             --backend-store-uri postgresql://aiplatform:AiPlatform2026Pg!@10.0.0.2:5432/mlflow
             --default-artifact-root s3://mlflow/artifacts"
```

```bash
cd /opt/ai-platform && docker compose up -d
```

### Step 8: Configure Nginx on ai-29

Create `/etc/nginx/sites-available/ai-platform`:

```nginx
server {
    listen 80;
    server_name <your-domain>;

    root /opt/ai-platform/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://10.0.0.2:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
        proxy_connect_timeout 10s;
    }

    location /auth/ {
        proxy_pass http://10.0.0.2:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
        proxy_connect_timeout 10s;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/ai-platform /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

### Step 9: Setup SSL with Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d <your-domain>
sudo certbot renew --dry-run
```

Certbot will auto-renew via systemd timer.

## Environment Variables Reference

### Common (All Services)

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | — | Must be `prod` in production |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/aiplatform` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `aiplatform` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `AiPlatform2026Pg!` | DB password |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis port |
| `NATS_URL` | `nats://localhost:4222` | NATS server URL |

### Service-Specific

| Variable | Service | Description |
|----------|---------|-------------|
| `MLFLOW_TRACKING_URI` | Lab | MLflow server URL (e.g. `http://mlflow:5000`) |
| `JWT_SECRET` | Gateway, Console | JWT signing key |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Gateway | Access token TTL in ms (default: 7200000 = 2h) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Gateway | Refresh token TTL in ms (default: 604800000 = 7d) |

## Gateway Routing (Production)

The gateway (Spring Cloud Gateway) routes requests based on path prefix:

| External Path | Target Service | Internal Address |
|---------------|----------------|-----------------|
| `/api/v1/auth/**` | Console | `http://10.0.0.2:8081` |
| `/api/v1/users/**` | Console | `http://10.0.0.2:8081` |
| `/api/v1/roles/**` | Console | `http://10.0.0.2:8081` |
| `/api/v1/tenants/**` | Console | `http://10.0.0.2:8081` |
| `/api/v1/projects/**` | Console | `http://10.0.0.2:8081` |
| `/api/v1/audit-logs/**` | Console | `http://10.0.0.2:8081` |
| `/api/v1/messages/**` | Console | `http://10.0.0.2:8081` |
| `/api/v1/clusters/**` | Operation | `http://10.0.0.2:8082` |
| `/api/v1/resource-quotas/**` | Operation | `http://10.0.0.2:8082` |
| `/api/v1/lab/**` | Lab | `http://10.0.0.3:8083` |
| `/api/v1/inference/**` | Inference | `http://10.0.0.3:8084` |
| `/api/v1/fastlabel/**` | FastLabel | `http://10.0.0.3:8085` |
| `/api/v1/scorecard/**` | Scorecard | `http://10.0.0.3:8086` |

## Database Migrations

Flyway manages all schema migrations. Migration files are in:
```
services/console/src/main/resources/db/migration/
├── V01__tenant_user_role.sql
├── V02__project_member.sql
├── V03__audit_log_message.sql
├── V04__cluster_resource_image_env.sql
├── V05__datasource_dataset.sql
├── V06__operator_workflow.sql
├── V07__experiment_model.sql
├── V08__inference_model_service.sql
├── V09__fastlabel_task_annotation.sql
├── V10__scorecard_variable_binning.sql
├── V11__feature_store.sql
├── V12__file_management.sql
├── V13__license.sql
├── V14__model_drift_evaluation.sql
├── V15__model_marketplace.sql
├── V16__quantization.sql
└── V17__annotation_team.sql
```

Migrations run automatically when the Console service starts (Flyway is enabled in `application-prod.yml`).

## MinIO Bucket Setup

After MinIO starts, create required buckets:

```bash
# Install mc (MinIO Client)
curl -fsSL https://dl.min.io/client/mc/release/linux-amd64/mc -o /usr/local/bin/mc
chmod +x /usr/local/bin/mc

# Configure alias
mc alias set minio http://10.0.0.2:9000 aiplatform AiPlatform2026Minio!

# Create buckets
mc mb minio/datasets
mc mb minio/models
mc mb minio/mlflow
mc mb minio/marketplace
mc mb minio/label-data
mc mb minio/exports
```

## Verification

After all services are running:

```bash
# Check gateway health
curl http://10.0.0.2:8080/actuator/health

# Login test
curl -X POST http://91.233.10.29/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'

# Check frontend
curl -I http://91.233.10.29/
```
