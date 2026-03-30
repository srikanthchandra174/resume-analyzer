# Quick Start Guide

## 5-Minute Quick Start (Docker)

### Prerequisites
- Docker & Docker Compose installed
- OpenAI API Key

### Steps

#### 1. Clone & Configure

```bash
# Clone repository
git clone <repository-url>
cd resume-analyzer

# Copy environment file
cp .env.example .env

# Edit .env with your OpenAI API key
# On Windows (Notepad)
notepad .env

# On macOS/Linux
nano .env
```

Set your API key:
```env
OPENAI_API_KEY=sk-your_actual_key_here
```

#### 2. Start Services

```bash
docker-compose up -d
```

Wait for all services to be ready:
```bash
docker-compose logs -f postgres
# Wait for "database system is ready to accept connections"
```

#### 3. Verify Installation

```bash
# Check health
curl http://localhost:8080/api/v1/resume-analysis/health

# Should return:
# Resume Analyzer API is up and running!
```

#### 4. Test API

```bash
# Create a test resume file or use a sample
# Then run:

curl -X POST http://localhost:8080/api/v1/resume-analysis/analyze \
  -F "resumeFile=@resume.pdf" \
  -F "jobDescription=Senior Java Developer with 5+ years of Spring Boot experience"
```

You should receive a JSON response with analysis results!

---

## 10-Minute Setup (Local Development)

### Prerequisites
- Java 21
- Maven 3.6+
- PostgreSQL 15+ with pgvector
- Redis 7+

### Steps

#### 1. Setup PostgreSQL with PGVector

```bash
# Windows (with WSL or Postgres installed)
psql -U postgres -c "CREATE DATABASE resume_analyzer;"
psql -U postgres -d resume_analyzer -c "CREATE EXTENSION vector;"

# Or with Docker
docker run --name postgres-pgvector \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=resume_analyzer \
  -p 5432:5432 \
  -d pgvector/pgvector:pg16
```

#### 2. Setup Redis

```bash
docker run --name redis \
  -p 6379:6379 \
  -d redis:7-alpine
```

#### 3. Clone & Build

```bash
git clone <repository-url>
cd resume-analyzer

# Build project
mvn clean install
```

#### 4. Set Environment Variable

```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY = "sk-your_key_here"

# macOS/Linux
export OPENAI_API_KEY="sk-your_key_here"
```

#### 5. Run Application

```bash
mvn spring-boot:run
```

Application starts on `http://localhost:8080`

---

## Testing the API

### Test 1: Analyze a Resume

```bash
# Create a sample resume file (resume.txt) or use a PDF

curl -X POST http://localhost:8080/api/v1/resume-analysis/analyze \
  -F "resumeFile=@resume.pdf" \
  -F "jobDescription=We are looking for a Senior Backend Engineer with expertise in Java, Spring Boot, and microservices. Required skills: Java 17+, Spring Boot 3+, Docker, Kubernetes, PostgreSQL. Nice to have: AWS, Redis, Apache Kafka."
```

### Test 2: Get Analysis Results

```bash
# Get analysis by ID (replace 1 with actual ID from response)
curl http://localhost:8080/api/v1/resume-analysis/1
```

### Test 3: View Analysis History

```bash
# Get first page of history
curl "http://localhost:8080/api/v1/resume-analysis/history?page=0&size=10"
```

### Test 4: Get Recent Analyses

```bash
# Get analyses from last 24 hours
curl "http://localhost:8080/api/v1/resume-analysis/recent?hours=24"
```

---

## Access Web UIs

Once Docker services are running:

### pgAdmin (PostgreSQL UI)
- URL: http://localhost:5050
- Email: admin@resumeanalyzer.com
- Password: admin

**Steps to connect to database:**
1. Login to pgAdmin
2. Click "Register" → "Server"
3. Name: "resume-analyzer"
4. Hostname: postgres
5. Port: 5432
6. Username: resumeuser
7. Password: resumepassword

### Redis Commander (Redis UI)
- URL: http://localhost:8081
- No login required

---

## Project Structure Quick Reference

```
resume-analyzer/
├── src/main/java/...
│   ├── controller/           REST endpoints
│   ├── service/              Business logic
│   ├── repository/           Database access
│   ├── entity/               Data models
│   ├── dto/                  API models
│   └── exception/            Error handling
├── src/main/resources/
│   ├── application.yml       Configuration
│   └── application-docker.yml Docker config
├── docker-compose.yml        Services setup
├── pom.xml                   Dependencies
└── README_IMPLEMENTATION.md  Full documentation
```

---

## Key Files to Know

| File | Purpose |
|------|---------|
| `src/main/resources/application.yml` | Application configuration |
| `docker-compose.yml` | Docker services definition |
| `.env.example` | Environment variables template |
| `init-db.sql` | Database schema |
| `API_DOCUMENTATION.md` | API endpoint details |
| `DEPLOYMENT_GUIDE.md` | Production deployment |

---

## Common Tasks

### Stop Services

```bash
# Keep data
docker-compose stop

# Or stop and remove containers
docker-compose down

# Stop and remove everything (careful!)
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f resume-analyzer
```

### Update Configuration

```bash
# Edit application.yml
nano src/main/resources/application.yml

# Restart (Docker)
docker-compose restart resume-analyzer

# Or (Local)
# Stop and run: mvn spring-boot:run
```

### Database Shell

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U resumeuser -d resume_analyzer

# Basic commands
\dt              # List tables
\l               # List databases
SELECT * FROM resume_analysis;  # Query data
\q               # Exit
```

### Redis Shell

```bash
# Connect to Redis
docker-compose exec redis redis-cli

# Basic commands
PING                # Test connection
KEYS *              # List all keys
GET key_name        # Get value
FLUSHALL            # Clear all data
```

---

## Troubleshooting

### "Connection refused" Error

```bash
# Check if services are running
docker-compose ps

# If not running, start them
docker-compose up -d

# Wait 10-15 seconds for services to initialize
sleep 15

# Try again
curl http://localhost:8080/api/v1/resume-analysis/health
```

### "OpenAI API Error"

```bash
# Verify API key is set
echo $OPENAI_API_KEY

# Check application logs
docker-compose logs -f resume-analyzer | grep -i openai

# Verify key format (should start with sk-)
```

### "PDF Extraction Failed"

```bash
# Ensure file is valid PDF
file resume.pdf

# Check file size (max 10MB)
ls -lh resume.pdf

# Verify MIME type
# Use a valid application/pdf MIME type
```

### Port Already in Use

```bash
# Change ports in docker-compose.yml
# Or stop other services using those ports

# Check what's using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Change in docker-compose.yml:
# ports:
#   - "8081:8080"  # Use 8081 instead
```

---

## Next Steps

1. **Read Full Documentation**: See `README_IMPLEMENTATION.md`
2. **Review API Docs**: See `API_DOCUMENTATION.md`
3. **Production Deployment**: See `DEPLOYMENT_GUIDE.md`
4. **Explore Code**: Start with `ResumeAnalysisController.java`

---

## Need Help?

- **API Issues**: Check `API_DOCUMENTATION.md`
- **Deployment**: See `DEPLOYMENT_GUIDE.md`
- **Code Structure**: Review the JavaDoc comments in source files
- **Bugs/Features**: Open GitHub issue

---

**Enjoy using Resume Analyzer!** 🚀

