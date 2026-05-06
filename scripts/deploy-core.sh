#!/usr/bin/env bash
set -euo pipefail

NODE_CORE="107.148.180.37"
REMOTE_DIR="/opt/ai-platform"
REPO_URL="${REPO_URL:-https://github.com/your-org/ai-platform.git}"
BRANCH="${BRANCH:-main}"

echo "==> Deploying to node-core ($NODE_CORE)"

ssh -o StrictHostKeyChecking=no root@$NODE_CORE bash -s <<REMOTE_SCRIPT
set -euo pipefail

if [ -d "$REMOTE_DIR" ]; then
    cd $REMOTE_DIR && git fetch origin && git reset --hard origin/$BRANCH
else
    git clone -b $BRANCH $REPO_URL $REMOTE_DIR
    cd $REMOTE_DIR
fi

echo "==> Building all services"
for svc in gateway console operation lab inference fastlabel scorecard; do
    echo "  Building \$svc..."
    docker build -t ai-platform/\$svc:latest -f infra/docker/Dockerfile.service services/\$svc
done

echo "==> Building frontend"
docker build -t ai-platform/frontend:latest frontend/

echo "==> Running Flyway migrations"
docker run --rm --network host \\
    -e FLYWAY_URL=jdbc:postgresql://\${POSTGRES_HOST:-localhost}:5432/aiplatform \\
    -e FLYWAY_USER=aiplatform \\
    -e FLYWAY_PASSWORD='AiPlatform2026Pg!' \\
    -v $REMOTE_DIR/infra/flyway/sql:/flyway/sql \\
    flyway/flyway:11 migrate

echo "==> Starting services"
cd $REMOTE_DIR/infra/docker
POSTGRES_HOST=\${POSTGRES_HOST:-localhost} \\
REDIS_HOST=\${REDIS_HOST:-localhost} \\
MINIO_ENDPOINT=\${MINIO_ENDPOINT:-http://localhost:9000} \\
NATS_URL=\${NATS_URL:-nats://localhost:4222} \\
docker compose up -d

echo "==> Waiting for services to become healthy"
sleep 15
docker compose ps

echo "==> Deploy complete"
REMOTE_SCRIPT

echo "==> Done"
