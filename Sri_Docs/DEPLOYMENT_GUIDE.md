# Deployment Guide

## Production Deployment

### Prerequisites

- Docker & Docker Compose installed
- OpenAI API Key
- Server with at least 2GB RAM
- 10GB free disk space

### Step 1: Prepare Environment

```bash
# Clone repository
git clone <repository-url>
cd resume-analyzer

# Copy environment template
cp .env.example .env

# Edit .env with production values
nano .env
```

### Step 2: Configure Production Variables

Edit `.env`:

```env
# PostgreSQL
POSTGRES_DB=resume_analyzer_prod
POSTGRES_USER=prod_user
POSTGRES_PASSWORD=strong_password_here

# OpenAI
OPENAI_API_KEY=sk-...

# Application
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080

# Security
PGADMIN_DEFAULT_PASSWORD=secure_password

# Logging
LOG_LEVEL=INFO
```

### Step 3: Build Docker Image

```bash
# Build with buildkit for better caching
DOCKER_BUILDKIT=1 docker build -t resume-analyzer:latest .

# Or build with docker-compose
docker-compose build
```

### Step 4: Deploy Services

```bash
# Start all services
docker-compose up -d

# Verify services are running
docker-compose ps

# Check logs
docker-compose logs -f
```

### Step 5: Database Migration

```bash
# Wait for PostgreSQL to be ready
docker-compose exec postgres pg_isready -U prod_user

# Initialize database
docker-compose exec postgres psql -U prod_user -d resume_analyzer_prod -f /docker-entrypoint-initdb.d/init-db.sql
```

### Step 6: Verify Deployment

```bash
# Check API health
curl http://localhost:8080/api/v1/resume-analysis/health

# Check database connection
curl http://localhost:8080/api/v1/resume-analysis/recent

# Access pgAdmin
open http://localhost:5050
```

---

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (v1.20+)
- kubectl configured
- Helm 3+

### Step 1: Create Secrets

```bash
# Create namespace
kubectl create namespace resume-analyzer

# Create secrets
kubectl create secret generic openai-secret \
  --from-literal=api-key=sk-... \
  -n resume-analyzer

kubectl create secret generic db-secret \
  --from-literal=username=prod_user \
  --from-literal=password=strong_password \
  -n resume-analyzer
```

### Step 2: Deploy PostgreSQL

```bash
# Add bitnami helm repo
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Install PostgreSQL with pgvector
helm install postgres bitnami/postgresql \
  --set auth.username=prod_user \
  --set auth.password=strong_password \
  --set auth.database=resume_analyzer_prod \
  -n resume-analyzer
```

### Step 3: Deploy Redis

```bash
helm install redis bitnami/redis \
  --set auth.enabled=false \
  -n resume-analyzer
```

### Step 4: Deploy Application

```bash
# Create deployment manifest
cat > deployment.yaml <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: resume-analyzer
  namespace: resume-analyzer
spec:
  replicas: 3
  selector:
    matchLabels:
      app: resume-analyzer
  template:
    metadata:
      labels:
        app: resume-analyzer
    spec:
      containers:
      - name: app
        image: resume-analyzer:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/resume_analyzer_prod
        - name: SPRING_DATA_REDIS_HOST
          value: redis-master
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: openai-secret
              key: api-key
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/v1/resume-analysis/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/v1/resume-analysis/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: resume-analyzer-service
  namespace: resume-analyzer
spec:
  selector:
    app: resume-analyzer
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
EOF

# Apply deployment
kubectl apply -f deployment.yaml
```

### Step 5: Verify Kubernetes Deployment

```bash
# Check pod status
kubectl get pods -n resume-analyzer

# View logs
kubectl logs -f deployment/resume-analyzer -n resume-analyzer

# Port forward for testing
kubectl port-forward svc/resume-analyzer-service 8080:80 -n resume-analyzer
```

---

## AWS Deployment (ECS/Fargate)

### Step 1: Create ECR Repository

```bash
aws ecr create-repository --repository-name resume-analyzer --region us-east-1
```

### Step 2: Build and Push Image

```bash
# Build image
docker build -t resume-analyzer:latest .

# Tag image
docker tag resume-analyzer:latest 123456789.dkr.ecr.us-east-1.amazonaws.com/resume-analyzer:latest

# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123456789.dkr.ecr.us-east-1.amazonaws.com

docker push 123456789.dkr.ecr.us-east-1.amazonaws.com/resume-analyzer:latest
```

### Step 3: Create RDS PostgreSQL

```bash
aws rds create-db-instance \
  --db-instance-identifier resume-analyzer-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username prod_user \
  --master-user-password strong_password \
  --allocated-storage 100 \
  --region us-east-1
```

### Step 4: Create ElastiCache Redis

```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id resume-analyzer-cache \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --region us-east-1
```

### Step 5: Create ECS Task Definition

```bash
# Create task-definition.json
cat > task-definition.json <<EOF
{
  "family": "resume-analyzer",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "resume-analyzer",
      "image": "123456789.dkr.ecr.us-east-1.amazonaws.com/resume-analyzer:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://resume-analyzer-db.c9akciq32.us-east-1.rds.amazonaws.com:5432/resume_analyzer_prod"
        },
        {
          "name": "SPRING_DATA_REDIS_HOST",
          "value": "resume-analyzer-cache.12345.ng.0001.use1.cache.amazonaws.com"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/resume-analyzer",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
EOF

# Register task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

### Step 6: Create ECS Service

```bash
aws ecs create-service \
  --cluster default \
  --service-name resume-analyzer \
  --task-definition resume-analyzer \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345],securityGroups=[sg-12345]}"
```

---

## Monitoring & Logging

### CloudWatch Logs

```bash
# View logs
aws logs tail /ecs/resume-analyzer --follow

# Get metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/ECS \
  --metric-name CPUUtilization \
  --dimensions Name=ServiceName,Value=resume-analyzer
```

### Health Checks

```bash
# Manual health check
curl http://your-load-balancer/api/v1/resume-analysis/health

# Automated with curl
watch -n 10 'curl -s http://your-load-balancer/api/v1/resume-analysis/health'
```

---

## Scaling

### Horizontal Scaling (Docker Compose)

```bash
# Scale application instances
docker-compose up -d --scale app=3

# Use load balancer (nginx)
# Configure nginx.conf to proxy to all instances
```

### Vertical Scaling

Increase resource limits in docker-compose.yml:

```yaml
services:
  resume-analyzer:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

---

## Backup & Recovery

### Database Backup

```bash
# Backup PostgreSQL
docker-compose exec postgres pg_dump -U prod_user resume_analyzer_prod > backup.sql

# Restore backup
docker-compose exec -T postgres psql -U prod_user resume_analyzer_prod < backup.sql
```

### Redis Backup

```bash
# Create backup
docker-compose exec redis redis-cli BGSAVE

# Copy backup file
docker cp resume-analyzer-redis:/data/dump.rdb ./redis-backup.rdb
```

---

## Maintenance

### Update Application

```bash
# Build new version
docker build -t resume-analyzer:v2 .

# Update docker-compose.yml image tag
sed -i 's/resume-analyzer:latest/resume-analyzer:v2/g' docker-compose.yml

# Restart services
docker-compose up -d
```

### Database Maintenance

```bash
# Vacuum database (optimize)
docker-compose exec postgres psql -U prod_user -d resume_analyzer_prod -c "VACUUM ANALYZE;"

# View connections
docker-compose exec postgres psql -U prod_user -d resume_analyzer_prod -c "SELECT count(*) FROM pg_stat_activity;"
```

### Clear Cache

```bash
# Flush Redis cache
docker-compose exec redis redis-cli FLUSHALL

# Or specific cache
docker-compose exec redis redis-cli --pattern 'resume_analysis:*' DELETE
```

---

## Troubleshooting

### Connection Issues

```bash
# Check database connectivity
docker-compose exec app curl -i postgres:5432

# Check Redis connectivity
docker-compose exec app redis-cli -h redis ping

# View container logs
docker-compose logs postgres redis app
```

### Performance Issues

```bash
# Check database slow queries
docker-compose exec postgres psql -U prod_user -d resume_analyzer_prod \
  -c "SELECT query, mean_time FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# Check Redis memory usage
docker-compose exec redis redis-cli INFO memory
```

---

## Production Checklist

- [ ] Set strong database password
- [ ] Set OpenAI API key
- [ ] Configure SSL/TLS certificate
- [ ] Set up monitoring and alerting
- [ ] Configure automated backups
- [ ] Set resource limits
- [ ] Configure health checks
- [ ] Set up log aggregation
- [ ] Configure rate limiting
- [ ] Implement authentication/authorization
- [ ] Set up CDN for static assets
- [ ] Configure auto-scaling policies
- [ ] Document runbooks
- [ ] Test disaster recovery

---

For more help, contact: devops@resumeanalyzer.com

