# Operations Manual

Day-to-day operations guide for AI Platform production environment.

## Service Management

### Service Locations

| Service | Server | Port | Internal Address |
|---------|--------|------|-----------------|
| Nginx | ai-29 (91.233.10.29) | 80/443 | localhost |
| Frontend | ai-29 | — | Static files via Nginx |
| Redis | ai-29 | 6379 | 10.0.0.1:6379 |
| NATS | ai-29 | 4222 | 10.0.0.1:4222 |
| Gateway | ai-v6a (10.0.0.2) | 8080 | 10.0.0.2:8080 |
| Console | ai-v6a | 8081 | 10.0.0.2:8081 |
| Operation | ai-v6a | 8082 | 10.0.0.2:8082 |
| PostgreSQL | ai-v6a | 5432 | 10.0.0.2:5432 |
| MinIO | ai-v6a | 9000/9001 | 10.0.0.2:9000 |
| Lab | ai-v6b (10.0.0.3) | 8083 | 10.0.0.3:8083 |
| Inference | ai-v6b | 8084 | 10.0.0.3:8084 |
| FastLabel | ai-v6b | 8085 | 10.0.0.3:8085 |
| Scorecard | ai-v6b | 8086 | 10.0.0.3:8086 |
| MLflow | ai-v6b | 5000 | 10.0.0.3:5000 |

### SSH Access

```bash
# SSH config (~/.ssh/config)
Host ai-29
    HostName 91.233.10.29
    User root
    IdentityFile ~/.ssh/id_ai

Host ai-v6a
    HostName 10.0.0.2
    User root
    ProxyJump ai-29
    IdentityFile ~/.ssh/id_ai

Host ai-v6b
    HostName 10.0.0.3
    User root
    ProxyJump ai-29
    IdentityFile ~/.ssh/id_ai
```

### Start / Stop / Restart

#### ai-29 (Redis, NATS, Nginx)

```bash
# Redis
docker start redis
docker stop redis
docker restart redis

# NATS
docker start nats
docker stop nats
docker restart nats

# Nginx
systemctl start nginx
systemctl stop nginx
systemctl reload nginx
systemctl restart nginx
```

#### ai-v6a (Gateway, Console, Operation)

```bash
cd /opt/ai-platform

# All services
docker compose up -d
docker compose down
docker compose restart

# Individual service
docker compose restart gateway
docker compose restart console
docker compose restart operation
docker compose logs -f gateway
```

#### ai-v6b (Lab, Inference, FastLabel, Scorecard, MLflow)

```bash
cd /opt/ai-platform

# All services
docker compose up -d
docker compose down
docker compose restart

# Individual service
docker compose restart lab
docker compose restart inference
docker compose restart fastlabel
docker compose restart scorecard
docker compose restart mlflow
```

### Start Order

When doing a full restart, follow this order:

1. **Infrastructure**: PostgreSQL → Redis → NATS → MinIO
2. **Console** (runs Flyway migrations)
3. **Gateway, Operation** (can start in parallel with Console)
4. **Lab, Inference, FastLabel, Scorecard, MLflow**
5. **Nginx** (or just reload)

### Health Check URLs

```bash
# Gateway
curl http://10.0.0.2:8080/actuator/health

# Console
curl http://10.0.0.2:8081/actuator/health

# Operation
curl http://10.0.0.2:8082/actuator/health

# Lab
curl http://10.0.0.3:8083/actuator/health

# Inference
curl http://10.0.0.3:8084/actuator/health

# FastLabel
curl http://10.0.0.3:8085/actuator/health

# Scorecard
curl http://10.0.0.3:8086/actuator/health

# MLflow
curl http://10.0.0.3:5000/health
```

Expected response:
```json
{"status":"UP"}
```

## Log Locations

### Docker Container Logs

```bash
# Follow logs for a specific service
docker compose logs -f gateway
docker compose logs -f console
docker compose logs -f lab --tail 200

# All services on the host
docker compose logs -f
```

### Application Logs

Spring Boot writes to stdout (captured by Docker). To write to file, add to `application-prod.yml`:

```yaml
logging:
  file:
    name: /var/log/ai-platform/gateway.log
```

### Nginx Logs

```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

## Database Operations

### Connection

```bash
# Direct connection (from ai-v6a)
docker exec -it postgresql psql -U aiplatform -d aiplatform

# Remote connection (from any server via WireGuard)
PGPASSWORD='AiPlatform2026Pg!' psql -h 10.0.0.2 -U aiplatform -d aiplatform

# Using Docker
docker exec -it postgresql psql -U aiplatform -d aiplatform -c "SELECT 1"
```

### Backup

```bash
# Full database dump (from ai-v6a)
docker exec postgresql pg_dump -U aiplatform aiplatform \
  | gzip > /opt/ai-platform/backups/aiplatform_$(date +%Y%m%d_%H%M%S).sql.gz

# MLflow database
docker exec postgresql pg_dump -U aiplatform mlflow \
  | gzip > /opt/ai-platform/backups/mlflow_$(date +%Y%m%d_%H%M%S).sql.gz

# Plain SQL backup (for inspection)
docker exec postgresql pg_dump -U aiplatform aiplatform \
  > /opt/ai-platform/backups/aiplatform_$(date +%Y%m%d).sql
```

### Restore

```bash
# From gzipped backup
gunzip -c /opt/ai-platform/backups/aiplatform_YYYYMMDD.sql.gz \
  | docker exec -i postgresql psql -U aiplatform -d aiplatform

# From plain SQL
docker exec -i postgresql psql -U aiplatform -d aiplatform \
  < /opt/ai-platform/backups/aiplatform_YYYYMMDD.sql
```

### Common SQL Queries

```sql
-- Check Flyway migration status
SELECT version, description, installed_on, success
FROM flyway_schema_history ORDER BY installed_rank;

-- List all tenants
SELECT id, name, status, created_at FROM tenants ORDER BY id;

-- Count users per tenant
SELECT t.name, COUNT(u.id) AS user_count
FROM tenants t LEFT JOIN users u ON u.tenant_id = t.id
GROUP BY t.id, t.name;

-- Recent audit logs
SELECT al.action, u.username, al.created_at, al.details
FROM audit_logs al JOIN users u ON al.user_id = u.id
ORDER BY al.created_at DESC LIMIT 20;

-- Active online inference services
SELECT id, name, model_id, status, replicas, release_type
FROM inference_online_service WHERE status = 'RUNNING';

-- Model archive status counts
SELECT status, COUNT(*) FROM lab_model_archive GROUP BY status;

-- Scorecard model metrics
SELECT id, name, status, ks_value, auc_value, gini_value, psi_value
FROM sc_model WHERE status = 'TRAINED';

-- Active labeling tasks
SELECT id, name, status, assigned_to, dataset_id
FROM label_task WHERE status IN ('PENDING', 'IN_PROGRESS');

-- Workflow runs (last 24h)
SELECT wr.id, w.name AS workflow_name, wr.status, wr.started_at, wr.finished_at
FROM workflow_run wr JOIN workflow w ON wr.workflow_id = w.id
WHERE wr.started_at > NOW() - INTERVAL '24 hours'
ORDER BY wr.started_at DESC;
```

### Create Database (First Time)

```bash
docker exec -it postgresql psql -U aiplatform -c "CREATE DATABASE aiplatform;"
docker exec -it postgresql psql -U aiplatform -c "CREATE DATABASE mlflow;"
```

## Monitoring

### Container Status

```bash
# On each server
docker compose ps
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### Resource Usage

```bash
# Per-container stats
docker stats --no-stream

# Disk usage
df -h
du -sh /opt/ai-platform/*

# Memory
free -h

# Docker disk usage
docker system df
```

### Log Tailing

```bash
# Follow all logs on ai-v6b
ssh ai-v6b "cd /opt/ai-platform && docker compose logs -f --tail 50"

# Follow a specific service
ssh ai-v6b "cd /opt/ai-platform && docker compose logs -f lab --tail 100"

# Grep for errors
ssh ai-v6a "cd /opt/ai-platform && docker compose logs gateway 2>&1 | grep -i error"
```

### WireGuard Status

```bash
# On ai-29 (hub)
sudo wg show wg0

# Check tunnel connectivity
ping -c 3 10.0.0.2
ping -c 3 10.0.0.3
```

### Nginx Status

```bash
# Test config
sudo nginx -t

# Active connections
curl -s http://localhost/nginx_status 2>/dev/null || ss -tlnp | grep :80

# Request rate
tail -1000 /var/log/nginx/access.log | awk '{print $1}' | sort | uniq -c | sort -rn | head
```

## Troubleshooting

### Common Issues

| Symptom | Possible Cause | Solution |
|---------|---------------|----------|
| 502 Bad Gateway | Backend service down | Check `docker compose ps` on ai-v6a, restart gateway |
| 502 on /api/v1/lab/** | Lab service down | Check ai-v6b: `docker compose restart lab` |
| Login fails (401) | Console not running or DB connection issue | Check Console logs, verify PostgreSQL is up |
| SSL certificate error | Cert expired | `sudo certbot renew` |
| WireGuard tunnel down | Service stopped or key mismatch | `sudo wg show wg0`, restart with `sudo systemctl restart wg-quick@wg0` |
| Service won't start | Port conflict or OOM | `docker compose logs <service>`, check `docker stats` |
| Flyway migration fails | Dirty schema | Check `flyway_schema_history`, may need `flyway repair` |
| MinIO connection refused | Container stopped | `docker restart minio` on ai-v6a |
| Redis timeout | Redis down or network | `docker restart redis` on ai-29, check WG tunnel |
| Slow API response | High memory/CPU usage | Check `docker stats`, increase `mem_limit` |
| Frontend blank page | Build artifacts missing | Rebuild frontend, copy dist/ to ai-29 |

### Service Won't Start

```bash
# 1. Check logs
docker compose logs <service-name>

# 2. Check if port is in use
ss -tlnp | grep <port>

# 3. Check DB connectivity
docker exec -it postgresql psql -U aiplatform -c "SELECT 1"

# 4. Check Redis
docker exec -it redis redis-cli ping

# 5. Check container status
docker inspect <container-name> | grep -A5 "State"
```

### Gateway Returns 502

```bash
# 1. Check if backend services are running
ssh ai-v6a "docker compose ps"
ssh ai-v6b "docker compose ps"

# 2. Test direct connection to backend
curl http://10.0.0.2:8081/actuator/health  # Console
curl http://10.0.0.3:8083/actuator/health  # Lab

# 3. Check gateway logs for routing errors
ssh ai-v6a "docker compose logs gateway --tail 50"

# 4. Verify WireGuard
ping -c 3 10.0.0.2
ping -c 3 10.0.0.3
```

### SSL Certificate Renewal

```bash
# Manual renewal
sudo certbot renew

# Force renewal (if needed)
sudo certbot renew --force-renewal

# Check certificate expiry
echo | openssl s_client -connect <your-domain>:443 2>/dev/null | openssl x509 -noout -dates

# Reload Nginx after renewal
sudo systemctl reload nginx
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker ps | grep postgresql

# Test connection
docker exec postgresql psql -U aiplatform -d aiplatform -c "SELECT 1"

# Check connection count
docker exec postgresql psql -U aiplatform -c "SELECT count(*) FROM pg_stat_activity"

# Kill idle connections
docker exec postgresql psql -U aiplatform -c \
  "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE state = 'idle' AND query_start < NOW() - INTERVAL '30 minutes'"
```

## Credentials Reference

### Database

| Parameter | Value |
|-----------|-------|
| Host | 10.0.0.2 (via WireGuard) |
| Port | 5432 |
| Database | `aiplatform` (main), `mlflow` (MLflow) |
| Username | `aiplatform` |
| Password | `AiPlatform2026Pg!` |

### MinIO

| Parameter | Value |
|-----------|-------|
| Endpoint | http://10.0.0.2:9000 |
| Console | http://10.0.0.2:9001 |
| Access Key | `aiplatform` |
| Secret Key | `AiPlatform2026Minio!` |

### JWT

| Parameter | Value |
|-----------|-------|
| Secret | `AiPlatformJwtSecretKey2024ForProductionUseMustBe256Bits!!` |
| Access Token TTL | 2 hours (7200000 ms) |
| Refresh Token TTL | 7 days (604800000 ms) |

### Admin Login

| Parameter | Value |
|-----------|-------|
| Username | `admin` |
| Password | `admin123` |

### Redis

| Parameter | Value |
|-----------|-------|
| Host | 10.0.0.1 |
| Port | 6379 |
| Password | (none) |

### NATS

| Parameter | Value |
|-----------|-------|
| URL | nats://10.0.0.1:4222 |
| Monitor | http://10.0.0.1:8222 |

### WireGuard

| Parameter | Value |
|-----------|-------|
| Hub Endpoint | 91.233.10.29:51820 |
| Subnet | 10.0.0.0/24 |
| ai-29 | 10.0.0.1 |
| ai-v6a | 10.0.0.2 |
| ai-v6b | 10.0.0.3 |

## Rolling Updates

### Update a Single Service

```bash
# 1. Build new JAR locally
./gradlew :services:lab:build -x test

# 2. Copy to target server
scp services/lab/build/libs/lab.jar root@10.0.0.3:/opt/ai-platform/lab.jar

# 3. Restart the container
ssh ai-v6b "cd /opt/ai-platform && docker compose restart lab"

# 4. Verify
ssh ai-v6b "curl -s http://localhost:8083/actuator/health"
```

### Full Platform Update

```bash
# 1. Build all
./gradlew build -x test

# 2. Distribute
scp services/{gateway,console,operation}/build/libs/*.jar root@10.0.0.2:/opt/ai-platform/
scp services/{lab,inference,fastlabel,scorecard}/build/libs/*.jar root@10.0.0.3:/opt/ai-platform/

# 3. Restart in order
ssh ai-v6a "cd /opt/ai-platform && docker compose restart console"
sleep 10
ssh ai-v6a "cd /opt/ai-platform && docker compose restart gateway operation"
ssh ai-v6b "cd /opt/ai-platform && docker compose restart"

# 4. Verify all
curl http://91.233.10.29/api/v1/auth/login -X POST \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

### Frontend Update

```bash
cd frontend && npm run build
scp -r dist/* root@91.233.10.29:/opt/ai-platform/frontend/dist/
ssh ai-29 "sudo systemctl reload nginx"
```
