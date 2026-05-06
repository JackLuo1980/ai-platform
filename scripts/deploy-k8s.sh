#!/usr/bin/env bash
set -euo pipefail

K3S_MASTER="38.14.196.83"
REMOTE_DIR="/opt/ai-platform"
REGISTRY="${REGISTRY:-localhost:5000}"
REPO_URL="${REPO_URL:-https://github.com/your-org/ai-platform.git}"
BRANCH="${BRANCH:-main}"

echo "==> Deploying to K3s ($K3S_MASTER)"

ssh -o StrictHostKeyChecking=no root@$K3S_MASTER bash -s <<REMOTE_SCRIPT
set -euo pipefail

if [ -d "$REMOTE_DIR" ]; then
    cd $REMOTE_DIR && git fetch origin && git reset --hard origin/$BRANCH
else
    git clone -b $BRANCH $REPO_URL $REMOTE_DIR
    cd $REMOTE_DIR
fi

echo "==> Building and pushing images"
for svc in gateway console operation lab inference fastlabel scorecard; do
    echo "  Building \$svc..."
    docker build -t $REGISTRY/ai-platform/\$svc:latest -f infra/docker/Dockerfile.service services/\$svc
    echo "  Pushing \$svc..."
    docker push $REGISTRY/ai-platform/\$svc:latest
done

echo "==> Applying K8s manifests"
kubectl apply -f infra/k8s/namespace.yaml

kubectl apply -f infra/k8s/gateway.yaml
kubectl apply -f infra/k8s/console.yaml
kubectl apply -f infra/k8s/operation.yaml
kubectl apply -f infra/k8s/lab.yaml
kubectl apply -f infra/k8s/inference.yaml
kubectl apply -f infra/k8s/fastlabel.yaml
kubectl apply -f infra/k8s/scorecard.yaml

echo "==> Waiting for rollouts"
for svc in gateway console operation lab inference fastlabel scorecard; do
    kubectl rollout status deployment/\$svc -n ai-platform-core --timeout=180s
done

echo "==> Verifying pods"
kubectl get pods -n ai-platform-core
kubectl get svc -n ai-platform-core

echo "==> Deploy complete"
REMOTE_SCRIPT

echo "==> Done"
