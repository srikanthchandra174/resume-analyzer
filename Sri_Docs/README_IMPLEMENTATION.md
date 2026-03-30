# AI Resume Analyzer

A production-grade Spring Boot application that uses Spring AI to analyze resumes against job descriptions. It extracts text from PDF resumes, compares them with job descriptions using AI, and provides structured analysis results.

## Features

✅ **PDF Resume Upload** - Upload PDF resumes via REST API  
✅ **Text Extraction** - Extract text from PDFs using Apache PDFBox  
✅ **AI-Powered Analysis** - Compare resume vs job description using Spring AI  
✅ **Structured Response** - Get match scores, missing skills, improvements, and interview questions  
✅ **Database Storage** - PostgreSQL with Spring Data JPA for persistence  
✅ **Vector Embeddings** - PGVector for semantic search capabilities  
✅ **Redis Caching** - Cache duplicate analysis requests for performance  
✅ **Layered Architecture** - Clean separation of concerns (Controller → Service → Repository)  
✅ **Exception Handling** - Global exception handler with detailed error responses  
✅ **Validation** - Input validation using Jakarta Validation  
✅ **Docker Support** - Docker Compose for easy deployment with PostgreSQL, Redis, pgAdmin, and Redis Commander  

## Tech Stack

- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21
- **Database**: PostgreSQL with PGVector extension
- **Cache**: Redis
- **AI Service**: Spring AI with OpenAI integration
- **PDF Processing**: Apache PDFBox 3.0.2
- **Build Tool**: Maven
- **ORM**: Hibernate JPA
- **Containerization**: Docker & Docker Compose

## Project Structure

```
resume-analyzer/
├── src/main/java/com/srikanth/ai/resumeanalyzer/
│   ├── controller/          # REST API endpoints
│   ├── service/             # Business logic
│   ├── repository/          # Data access layer
│   ├── entity/              # JPA entities
│   ├── dto/                 # Data transfer objects
│   ├── exception/           # Custom exceptions & global handler
│   └── config/              # Spring configurations
├── src/main/resources/
│   ├── application.yml      # Main configuration
│   ├── application-docker.yml  # Docker profile
│   └── application.properties
├── docker-compose.yml       # Docker services orchestration
├── Dockerfile               # Container image definition
├── init-db.sql             # Database initialization script
├── pom.xml                 # Maven dependencies
└── README.md               # This file
```

## Prerequisites

- **Java 21 or higher**
- **Maven 3.6+**
- **Docker & Docker Compose** (for containerized deployment)
- **OpenAI API Key** (for AI analysis)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd resume-analyzer
```

### 2. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and set your **OpenAI API Key**:

```env
OPENAI_API_KEY=your_actual_openai_key_here
```

### 3. Option A: Local Development

#### Prerequisites for Local Setup
- PostgreSQL 15+ with pgvector extension
- Redis 7+

#### Start PostgreSQL and Redis

```bash
docker-compose up -d postgres redis pgadmin redis-commander
```

#### Build and Run Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Option B: Docker Deployment (Recommended)

Start all services together:

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** (port 5432)
- **Redis** (port 6379)
- **Redis Commander** (port 8081)
- **pgAdmin** (port 5050)
- **Application** (port 8080)

Build and run the application container:

```bash
docker build -t resume-analyzer:latest .
docker-compose -f docker-compose.yml up -d
```

## API Endpoints

### 1. Analyze Resume

**Request:**
```http
POST /api/v1/resume-analysis/analyze
Content-Type: multipart/form-data

Parameters:
- resumeFile (MultipartFile): PDF resume file
- jobDescription (String): Job description text
```

**Response:**
```json
{
  "analysisId": 1,
  "matchScore": 85.5,
  "missingSkills": ["Kubernetes", "Docker", "AWS"],
  "suggestedImprovements": [
    "Add more project details",
    "Highlight leadership experience",
    "Include specific metrics and achievements"
  ],
  "interviewQuestions": [
    "Can you describe your experience with microservices?",
    "How do you approach system design?",
    "Tell us about a challenging project you worked on"
  ],
  "summary": "Your resume is a good match for this position with a focus on core skills. Consider adding more details about cloud infrastructure experience."
}
```

### 2. Get Analysis by ID

**Request:**
```http
GET /api/v1/resume-analysis/{id}
```

**Response:**
```json
{
  "analysisId": 1,
  "matchScore": 85.5,
  "missingSkills": ["Kubernetes", "Docker"],
  "suggestedImprovements": ["Add more project details"],
  "interviewQuestions": ["Can you describe your experience?"],
  "summary": "Good match for this position"
}
```

### 3. Get Analysis History (Paginated)

**Request:**
```http
GET /api/v1/resume-analysis/history?page=0&size=10&sort=createdAt,desc
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "jobTitle": "Job Analysis",
      "matchScore": 85.5,
      "createdAt": 1711785600000,
      "fileName": "resume.pdf"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 25,
  "totalPages": 3
}
```

### 4. Get Recent Analyses

**Request:**
```http
GET /api/v1/resume-analysis/recent?hours=24
```

**Response:**
```json
[
  {
    "id": 1,
    "jobTitle": "Job Analysis",
    "matchScore": 85.5,
    "createdAt": 1711785600000,
    "fileName": "resume.pdf"
  }
]
```

### 5. Health Check

**Request:**
```http
GET /api/v1/resume-analysis/health
```

**Response:**
```
Resume Analyzer API is up and running!
```

## Configuration Details

### application.yml

Key configurations:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/resume_analyzer
    username: resumeuser
    password: resumepassword
  
  data:
    redis:
      host: localhost
      port: 6379
  
  servlet:
    multipart:
      max-file-size: 10MB
  
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4-turbo-preview
          temperature: 0.7
```

### Database Schema

Key tables:
- **resume_analysis**: Main analysis records
- **missing_skills**: Skills gap analysis
- **suggested_improvements**: Improvement recommendations
- **interview_questions**: Generated interview questions
- **resume_embeddings**: Vector embeddings for semantic search

## Error Handling

All errors return structured JSON responses:

```json
{
  "error_code": "VALIDATION_ERROR",
  "message": "Input validation failed",
  "timestamp": 1711785600000,
  "path": "/api/v1/resume-analysis/analyze",
  "error_details": "resumeFile: File cannot be empty"
}
```

### Error Codes

| Code | Status | Description |
|------|--------|-------------|
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `PDF_EXTRACTION_ERROR` | 400 | PDF file processing failed |
| `FILE_SIZE_EXCEEDED` | 413 | File exceeds maximum size |
| `RESOURCE_NOT_FOUND` | 404 | Requested resource not found |
| `ANALYSIS_ERROR` | 500 | AI analysis failed |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected error |

## Service Management

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### View Logs

```bash
# Application logs
docker-compose logs -f resume-analyzer

# Database logs
docker-compose logs -f postgres

# Redis logs
docker-compose logs -f redis
```

### Access Management Interfaces

- **pgAdmin**: http://localhost:5050 (admin@resumeanalyzer.com / admin)
- **Redis Commander**: http://localhost:8081

## Performance Optimization

1. **Caching**: Redis caches duplicate analysis requests for 24 hours
2. **Database Indexing**: Strategic indexes on frequently queried columns
3. **Connection Pooling**: HikariCP for database connection management
4. **Vector Search**: PGVector for semantic similarity search
5. **Request Compression**: Gzip compression enabled for responses

## Security Considerations

1. **Input Validation**: All inputs validated using Jakarta Validation
2. **File Upload Limits**: Maximum 10MB PDF files
3. **MIME Type Validation**: Only PDF files accepted
4. **SQL Injection Prevention**: Parameterized queries via JPA
5. **CORS Configuration**: Cross-origin requests allowed (production: restrict origins)
6. **Non-root User**: Docker container runs as non-root user

## Future Enhancements

- [ ] React frontend integration
- [ ] User authentication and authorization
- [ ] Advanced resume parsing with named entity recognition
- [ ] Salary estimation based on skills
- [ ] Resume templates and suggestions
- [ ] Bulk resume analysis
- [ ] Integration with job portals (LinkedIn, Indeed)
- [ ] Machine learning model for match score prediction

## Troubleshooting

### PostgreSQL Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# View PostgreSQL logs
docker-compose logs postgres

# Verify connection
docker exec resume-analyzer-postgres psql -U resumeuser -d resume_analyzer -c "\dt"
```

### Redis Connection Issues

```bash
# Check Redis status
docker-compose logs redis

# Test Redis connectivity
docker exec resume-analyzer-redis redis-cli ping
```

### OpenAI API Errors

```bash
# Verify API key is set
echo $OPENAI_API_KEY

# Check API key validity in application logs
docker-compose logs resume-analyzer | grep -i openai
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact & Support

- **Email**: support@resumeanalyzer.com
- **Issues**: GitHub Issues
- **Documentation**: See HELP.md

---

**Built with ❤️ using Spring Boot and Spring AI**

