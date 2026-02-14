---
Last Updated: 2026-02-14T18:00:00Z
Updated By: davidparker-lv-bmth
---

# DMS Operations Runbook

## Table of Contents

1. [Service Health Monitoring](#service-health-monitoring)
2. [Common Operational Tasks](#common-operational-tasks)
3. [Troubleshooting](#troubleshooting)
4. [Backup & Recovery](#backup--recovery)
5. [Scaling](#scaling)
6. [Incident Response](#incident-response)

---

## Service Health Monitoring

### Health Check Endpoints

```bash
# Check all services
curl http://localhost:8081/actuator/health  # Admin
curl http://localhost:8082/actuator/health  # Audit
curl http://localhost:8083/actuator/health  # Document
curl http://localhost:8084/actuator/health  # Compliance
curl http://localhost:8085/actuator/health  # LLM
curl http://localhost:8080/actuator/health  # Gateway
```

### Key Metrics to Monitor

| Metric | Threshold | Action |
|--------|-----------|--------|
| Response time p99 | > 5s | Investigate slow queries |
| Error rate | > 1% | Check logs for exceptions |
| Database connections | > 80% pool | Scale or optimize |
| Memory usage | > 85% | Increase JVM heap |
| Disk usage | > 80% | Clean up or expand |
| Audit log volume | > 10k/hour | Review for noisy services |

### Log Locations

- **Docker Compose**: `docker compose logs -f [service-name]`
- **Kubernetes**: `kubectl logs -f deployment/[service-name] -n dms`
- **Application logs**: Follow `logging.pattern.console` in application.yml

---

## Common Operational Tasks

### Restarting Services

```bash
# Docker Compose
docker compose restart dms-admin-service
docker compose restart dms-document-service

# Kubernetes
kubectl rollout restart deployment/dms-admin-service -n dms
```

### Viewing Audit Logs

```bash
# Via API
curl "http://localhost:8082/api/v1/audit/logs?page=0&size=20"

# Filter by event type
curl "http://localhost:8082/api/v1/audit/logs?eventType=CREATE"

# Filter by time range
curl "http://localhost:8082/api/v1/audit/logs?startTime=2026-02-14T00:00:00Z&endTime=2026-02-15T00:00:00Z"
```

### Database Maintenance

```bash
# Connect to admin database
psql -h localhost -p 5432 -U postgres -d dms_admin

# Check table sizes
SELECT relname, pg_size_pretty(pg_total_relation_size(relid))
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;

# Vacuum audit logs (heavy table)
VACUUM ANALYZE audit_logs;
```

### Exporting Data

```bash
# Export users as CSV
curl http://localhost:8081/api/v1/admin/export/users/csv > users.csv

# Export audit logs
curl http://localhost:8081/api/v1/admin/export/audit/csv > audit.csv

# Export compliance report
curl http://localhost:8081/api/v1/admin/export/compliance/report > compliance.csv
```

---

## Troubleshooting

### Service Won't Start

1. Check logs: `docker compose logs dms-[service]-service | tail -50`
2. Verify database is running: `docker compose ps postgres-*`
3. Check environment variables: `docker compose config`
4. Verify port not in use: `lsof -i :808X`

### Azure Key Vault Errors

**Symptom**: `CredentialUnavailableException` on startup
**Fix**: Ensure using correct profile (dev/docker bypasses Azure)
```bash
# Set profile
SPRING_PROFILES_ACTIVE=docker docker compose up -d
```

### Database Connection Issues

**Symptom**: `Connection refused` or `too many connections`
```bash
# Check connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'dms_admin';

# Kill idle connections
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'dms_admin' AND state = 'idle' AND query_start < NOW() - INTERVAL '1 hour';
```

### Audit Log Partition Management

Audit logs use PostgreSQL range partitioning by month. Create new partitions monthly:
```sql
-- Create next month's partition
CREATE TABLE audit_logs_2026_03 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
```

### High Memory Usage

1. Check JVM heap: `jcmd <pid> GC.heap_info`
2. Trigger GC: `jcmd <pid> GC.run`
3. Increase heap: Set `JAVA_OPTS=-Xmx1g` in environment

---

## Backup & Recovery

### Database Backup

```bash
# Full backup
pg_dump -h localhost -U postgres dms_admin > dms_admin_backup.sql
pg_dump -h localhost -U postgres dms_document > dms_document_backup.sql
pg_dump -h localhost -U postgres dms_audit > dms_audit_backup.sql

# Restore
psql -h localhost -U postgres dms_admin < dms_admin_backup.sql
```

### Scheduled Backups (Kubernetes CronJob)

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: dms-db-backup
spec:
  schedule: "0 2 * * *"  # 2 AM daily
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: postgres:16
            command: ["pg_dumpall", "-h", "postgres", "-U", "postgres"]
```

---

## Scaling

### Horizontal Scaling

```bash
# Kubernetes
kubectl scale deployment/dms-document-service --replicas=3 -n dms
kubectl scale deployment/dms-admin-service --replicas=2 -n dms
```

### Database Connection Pooling

Each service configured with HikariCP:
- Admin: max 10 connections
- Document: max 20 connections  
- Audit: max 10 connections

Adjust in `application.yml`:
```yaml
spring.datasource.hikari.maximum-pool-size: 20
```

---

## Incident Response

### Severity Levels

| Level | Description | Response Time |
|-------|------------|---------------|
| P1 | Service completely down | 15 minutes |
| P2 | Degraded performance | 1 hour |
| P3 | Non-critical feature broken | 4 hours |
| P4 | Cosmetic/documentation | Next sprint |

### Response Checklist

1. **Identify**: Which service? What error? When did it start?
2. **Communicate**: Notify stakeholders
3. **Diagnose**: Check logs, metrics, recent deployments
4. **Mitigate**: Restart service, rollback, scale up
5. **Resolve**: Fix root cause
6. **Review**: Post-incident review, update runbook
