# Resume Analyzer API Documentation

## Base URL

```
http://localhost:8080/api/v1/resume-analysis
```

## Authentication

Currently, the API does not require authentication. For production deployment, implement OAuth2 or JWT authentication.

---

## Endpoints

### 1. Analyze Resume

Analyzes a resume against a job description.

**Endpoint:** `POST /analyze`

**Content-Type:** `multipart/form-data`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `resumeFile` | File | Yes | PDF resume file (max 10MB) |
| `jobDescription` | String | Yes | Job description text (min 50 characters) |

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/v1/resume-analysis/analyze \
  -F "resumeFile=@resume.pdf" \
  -F "jobDescription=We are looking for a Senior Java Developer with 5+ years of experience in Spring Boot..."
```

**Success Response (200):**

```json
{
  "analysisId": 1,
  "matchScore": 85.5,
  "missingSkills": [
    "Kubernetes",
    "Docker",
    "AWS Lambda"
  ],
  "suggestedImprovements": [
    "Add more details about containerization experience",
    "Highlight cloud infrastructure expertise",
    "Include more quantifiable metrics and achievements"
  ],
  "interviewQuestions": [
    "Can you describe your experience with microservices architecture?",
    "Tell us about your involvement in container orchestration",
    "How do you approach system scalability?"
  ],
  "summary": "Your resume demonstrates strong Java development skills with experience in Spring Boot. To improve the match, consider adding more details about cloud infrastructure and containerization technologies."
}
```

**Error Response Examples:**

**400 - Validation Error:**
```json
{
  "error_code": "VALIDATION_ERROR",
  "message": "Input validation failed",
  "timestamp": 1711785600000,
  "path": "/api/v1/resume-analysis/analyze",
  "error_details": "jobDescription: must not be blank"
}
```

**413 - File Size Exceeded:**
```json
{
  "error_code": "FILE_SIZE_EXCEEDED",
  "message": "File size exceeds maximum allowed limit",
  "timestamp": 1711785600000,
  "path": "/api/v1/resume-analysis/analyze"
}
```

**500 - Analysis Error:**
```json
{
  "error_code": "ANALYSIS_ERROR",
  "message": "Failed to analyze resume: Connection timeout to AI service",
  "timestamp": 1711785600000,
  "path": "/api/v1/resume-analysis/analyze"
}
```

---

### 2. Get Analysis by ID

Retrieves a specific analysis result by its ID.

**Endpoint:** `GET /{id}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Long | Yes | Analysis ID |

**Example Request:**

```bash
curl -X GET http://localhost:8080/api/v1/resume-analysis/1
```

**Success Response (200):**

```json
{
  "analysisId": 1,
  "matchScore": 85.5,
  "missingSkills": ["Kubernetes", "Docker"],
  "suggestedImprovements": ["Add containerization experience"],
  "interviewQuestions": ["Tell us about your microservices experience"],
  "summary": "Good match for the position"
}
```

**Error Response (404):**

```json
{
  "error_code": "RESOURCE_NOT_FOUND",
  "message": "Analysis with ID 999 not found",
  "timestamp": 1711785600000,
  "path": "/api/v1/resume-analysis/999"
}
```

---

### 3. Get Analysis History (Paginated)

Retrieves paginated list of all analysis records.

**Endpoint:** `GET /history`

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 10 | Number of records per page |
| `sort` | String | No | createdAt,desc | Sort criteria (e.g., "matchScore,desc") |

**Example Requests:**

```bash
# Get first 10 records
curl -X GET "http://localhost:8080/api/v1/resume-analysis/history"

# Get page 2 with 20 records per page, sorted by matchScore descending
curl -X GET "http://localhost:8080/api/v1/resume-analysis/history?page=1&size=20&sort=matchScore,desc"

# Sort by creation date ascending
curl -X GET "http://localhost:8080/api/v1/resume-analysis/history?sort=createdAt,asc"
```

**Success Response (200):**

```json
{
  "content": [
    {
      "id": 1,
      "jobTitle": "Job Analysis",
      "matchScore": 85.5,
      "createdAt": 1711785600000,
      "fileName": "resume.pdf"
    },
    {
      "id": 2,
      "jobTitle": "Job Analysis",
      "matchScore": 72.3,
      "createdAt": 1711695600000,
      "fileName": "resume_updated.pdf"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": [
      {
        "property": "createdAt",
        "direction": "DESC",
        "ignoreCase": false,
        "nullHandling": "NATIVE",
        "ascending": false
      }
    ]
  },
  "totalElements": 25,
  "totalPages": 3,
  "numberOfElements": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

---

### 4. Get Recent Analyses

Retrieves analyses created within the specified number of hours.

**Endpoint:** `GET /recent`

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `hours` | Integer | No | 24 | Number of hours to look back |

**Example Requests:**

```bash
# Get analyses from last 24 hours (default)
curl -X GET http://localhost:8080/api/v1/resume-analysis/recent

# Get analyses from last 7 days
curl -X GET "http://localhost:8080/api/v1/resume-analysis/recent?hours=168"

# Get analyses from last 1 hour
curl -X GET "http://localhost:8080/api/v1/resume-analysis/recent?hours=1"
```

**Success Response (200):**

```json
[
  {
    "id": 1,
    "jobTitle": "Job Analysis",
    "matchScore": 85.5,
    "createdAt": 1711785600000,
    "fileName": "resume.pdf"
  },
  {
    "id": 2,
    "jobTitle": "Job Analysis",
    "matchScore": 72.3,
    "createdAt": 1711695600000,
    "fileName": "resume_updated.pdf"
  }
]
```

---

### 5. Health Check

Verifies that the API is running and accessible.

**Endpoint:** `GET /health`

**Example Request:**

```bash
curl -X GET http://localhost:8080/api/v1/resume-analysis/health
```

**Success Response (200):**

```
Resume Analyzer API is up and running!
```

---

## Response Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Request successful |
| 400 | Bad Request | Invalid input or validation error |
| 404 | Not Found | Resource not found |
| 413 | Payload Too Large | File size exceeds limit |
| 500 | Internal Server Error | Server error |

---

## Error Codes Reference

| Error Code | HTTP Status | Description | Resolution |
|-----------|------------|-------------|-----------|
| `VALIDATION_ERROR` | 400 | Input validation failed | Check request parameters |
| `PDF_EXTRACTION_ERROR` | 400 | PDF processing failed | Ensure file is valid PDF |
| `FILE_SIZE_EXCEEDED` | 413 | File exceeds max size (10MB) | Upload smaller file |
| `RESOURCE_NOT_FOUND` | 404 | Resource not found | Use valid ID |
| `ANALYSIS_ERROR` | 500 | AI analysis failed | Check API key and retry |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected error | Contact support |

---

## Rate Limiting

Currently, no rate limiting is implemented. For production:
- Implement token bucket algorithm
- Limit requests to 100/minute per IP
- Implement exponential backoff for retries

---

## Caching Behavior

- Analysis results are cached for 24 hours
- Cache key is generated from resume text + job description hash
- Identical resume/JD combinations return cached results
- Cache is automatically invalidated after 24 hours

---

## Example Workflows

### Workflow 1: Complete Analysis Flow

```bash
# 1. Analyze resume
ANALYSIS_ID=$(curl -X POST http://localhost:8080/api/v1/resume-analysis/analyze \
  -F "resumeFile=@resume.pdf" \
  -F "jobDescription=Senior Java Developer..." \
  -s | jq '.analysisId')

# 2. Retrieve specific analysis
curl -X GET http://localhost:8080/api/v1/resume-analysis/$ANALYSIS_ID

# 3. Get analysis history
curl -X GET "http://localhost:8080/api/v1/resume-analysis/history?size=5"

# 4. Get recent analyses
curl -X GET "http://localhost:8080/api/v1/resume-analysis/recent?hours=24"
```

---

## Integration Examples

### JavaScript/Node.js

```javascript
const FormData = require('form-data');
const fs = require('fs');
const axios = require('axios');

async function analyzeResume() {
  const form = new FormData();
  form.append('resumeFile', fs.createReadStream('resume.pdf'));
  form.append('jobDescription', 'Senior Java Developer position...');
  
  try {
    const response = await axios.post(
      'http://localhost:8080/api/v1/resume-analysis/analyze',
      form,
      { headers: form.getHeaders() }
    );
    console.log('Analysis:', response.data);
  } catch (error) {
    console.error('Error:', error.response.data);
  }
}
```

### Python

```python
import requests

def analyze_resume():
    url = 'http://localhost:8080/api/v1/resume-analysis/analyze'
    
    with open('resume.pdf', 'rb') as f:
        files = {'resumeFile': f}
        data = {'jobDescription': 'Senior Java Developer position...'}
        
        response = requests.post(url, files=files, data=data)
        print('Analysis:', response.json())

if __name__ == '__main__':
    analyze_resume()
```

### cURL (Bash)

```bash
#!/bin/bash

RESUME_FILE="resume.pdf"
JOB_DESC="Senior Java Developer with 5+ years Spring Boot experience..."
API_URL="http://localhost:8080/api/v1/resume-analysis"

# Analyze resume
curl -X POST "$API_URL/analyze" \
  -F "resumeFile=@$RESUME_FILE" \
  -F "jobDescription=$JOB_DESC" \
  -H "Accept: application/json"
```

---

## Best Practices

1. **Always validate** resume files before upload
2. **Use reasonable** job descriptions (50-2000 characters)
3. **Implement retry logic** with exponential backoff
4. **Cache results** on client side when possible
5. **Handle errors gracefully** with user-friendly messages
6. **Log all API calls** for debugging and monitoring

---

## Support & Issues

For issues or feature requests, please contact: support@resumeanalyzer.com

