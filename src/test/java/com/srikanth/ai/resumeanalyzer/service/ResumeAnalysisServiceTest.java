package com.srikanth.ai.resumeanalyzer.service;

import com.srikanth.ai.resumeanalyzer.dto.ResumeAnalysisResponse;
import com.srikanth.ai.resumeanalyzer.entity.ResumeAnalysis;
import com.srikanth.ai.resumeanalyzer.exception.ResourceNotFoundException;
import com.srikanth.ai.resumeanalyzer.repository.ResumeAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResumeAnalysisService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeAnalysisService Tests")
class ResumeAnalysisServiceTest {

    private ResumeAnalysisService resumeAnalysisService;

    @Mock
    private PdfExtractionService pdfExtractionService;

    @Mock
    private AiAnalysisService aiAnalysisService;

    @Mock
    private CacheService cacheService;

    @Mock
    private ResumeAnalysisRepository repository;

    @BeforeEach
    void setUp() {
        resumeAnalysisService = new ResumeAnalysisService(
            pdfExtractionService,
            aiAnalysisService,
            cacheService,
            repository
        );
    }

    @Test
    @DisplayName("analyzeResume should return cached response without calling AI or saving")
    void testAnalyzeResumeWithCacheHit() {
        // Setup
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "bytes".getBytes());
        String jobDescription = "Job description";
        String resumeText = "Resume text";

        when(pdfExtractionService.extractTextFromPdf(file)).thenReturn(resumeText);
        when(cacheService.generateCacheKey(resumeText, jobDescription)).thenReturn("cache_key_123");

        Map<String, Object> cachedResult = new HashMap<>();
        cachedResult.put("matchScore", 85.0);
        cachedResult.put("summary", "Cached summary");
        cachedResult.put("missingSkills", Arrays.asList("Skill1", "Skill2"));
        cachedResult.put("suggestedImprovements", new ArrayList<>());
        cachedResult.put("interviewQuestions", new ArrayList<>());

        when(cacheService.getCachedAnalysis("cache_key_123")).thenReturn(cachedResult);

        // Execute
        ResumeAnalysisResponse response = resumeAnalysisService.analyzeResume(file, jobDescription);

        // Verify
        assertNotNull(response);
        assertEquals(85.0, response.getMatchScore());
        assertEquals("Cached summary", response.getSummary());
        assertEquals(2, response.getMissingSkills().size());

        // Verify AI and repository were not called
        verify(aiAnalysisService, never()).analyzeResumeAgainstJobDescription(anyString(), anyString());
        verify(repository, never()).save(any());
        verify(cacheService, never()).cacheAnalysis(anyString(), anyMap());
    }

    @Test
    @DisplayName("analyzeResume should call AI analysis on cache miss")
    void testAnalyzeResumeWithCacheMiss() {
        // Setup
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "bytes".getBytes());
        String jobDescription = "Job description";
        String resumeText = "Resume text";

        when(pdfExtractionService.extractTextFromPdf(file)).thenReturn(resumeText);
        when(cacheService.generateCacheKey(resumeText, jobDescription)).thenReturn("cache_key_123");
        when(cacheService.getCachedAnalysis("cache_key_123")).thenReturn(null); // Cache miss

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("matchScore", 90.0);
        aiResult.put("summary", "AI summary");
        aiResult.put("missingSkills", Arrays.asList("Kubernetes"));
        aiResult.put("suggestedImprovements", Arrays.asList("Add cloud skills"));
        aiResult.put("interviewQuestions", Arrays.asList("Tell me about microservices"));

        when(aiAnalysisService.analyzeResumeAgainstJobDescription(resumeText, jobDescription))
            .thenReturn(aiResult);

        ResumeAnalysis savedEntity = ResumeAnalysis.builder()
            .id(1L)
            .fileName("resume.pdf")
            .resumeText(resumeText)
            .jobDescription(jobDescription)
            .matchScore(90.0)
            .summary("AI summary")
            .missingSkills(Arrays.asList("Kubernetes"))
            .suggestedImprovements(Arrays.asList("Add cloud skills"))
            .interviewQuestions(Arrays.asList("Tell me about microservices"))
            .createdAt(LocalDateTime.now())
            .build();

        when(repository.save(any(ResumeAnalysis.class))).thenReturn(savedEntity);

        // Execute
        ResumeAnalysisResponse response = resumeAnalysisService.analyzeResume(file, jobDescription);

        // Verify
        assertNotNull(response);
        assertEquals(1L, response.getAnalysisId());
        assertEquals(90.0, response.getMatchScore());
        assertEquals("AI summary", response.getSummary());
        assertEquals(1, response.getMissingSkills().size());
        assertEquals("Kubernetes", response.getMissingSkills().get(0));

        // Verify calls were made
        verify(aiAnalysisService).analyzeResumeAgainstJobDescription(resumeText, jobDescription);
        verify(repository).save(any(ResumeAnalysis.class));
        verify(cacheService).cacheAnalysis("cache_key_123", aiResult);
    }

    @Test
    @DisplayName("analyzeResume should persist correct entity fields")
    void testAnalyzeResumePersistsCorrectFields() {
        // Setup
        MultipartFile file = new MockMultipartFile("file", "my_resume.pdf", "application/pdf", "bytes".getBytes());
        String jobDescription = "Senior Backend Engineer";
        String resumeText = "10 years Java experience";

        when(pdfExtractionService.extractTextFromPdf(file)).thenReturn(resumeText);
        when(cacheService.generateCacheKey(resumeText, jobDescription)).thenReturn("key");
        when(cacheService.getCachedAnalysis("key")).thenReturn(null);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("matchScore", 87.0);
        aiResult.put("summary", "Test");
        aiResult.put("missingSkills", new ArrayList<>());
        aiResult.put("suggestedImprovements", new ArrayList<>());
        aiResult.put("interviewQuestions", new ArrayList<>());

        when(aiAnalysisService.analyzeResumeAgainstJobDescription(resumeText, jobDescription))
            .thenReturn(aiResult);

        ResumeAnalysis savedEntity = ResumeAnalysis.builder()
            .id(1L)
            .fileName("my_resume.pdf")
            .resumeText(resumeText)
            .jobDescription(jobDescription)
            .matchScore(87.0)
            .build();

        ArgumentCaptor<ResumeAnalysis> captor = ArgumentCaptor.forClass(ResumeAnalysis.class);
        when(repository.save(captor.capture())).thenReturn(savedEntity);

        // Execute
        resumeAnalysisService.analyzeResume(file, jobDescription);

        // Verify
        ResumeAnalysis persistedEntity = captor.getValue();
        assertEquals("my_resume.pdf", persistedEntity.getFileName());
        assertEquals(resumeText, persistedEntity.getResumeText());
        assertEquals(jobDescription, persistedEntity.getJobDescription());
        assertEquals(87.0, persistedEntity.getMatchScore());
    }

    @Test
    @DisplayName("getAnalysisById should map entity to response")
    void testGetAnalysisById() {
        // Setup
        ResumeAnalysis entity = ResumeAnalysis.builder()
            .id(1L)
            .fileName("resume.pdf")
            .matchScore(85.0)
            .summary("Test summary")
            .missingSkills(Arrays.asList("Docker"))
            .suggestedImprovements(Arrays.asList("Improve skills"))
            .interviewQuestions(Arrays.asList("Question 1"))
            .build();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // Execute
        ResumeAnalysisResponse response = resumeAnalysisService.getAnalysisById(1L);

        // Verify
        assertNotNull(response);
        assertEquals(1L, response.getAnalysisId());
        assertEquals(85.0, response.getMatchScore());
        assertEquals("Test summary", response.getSummary());
        assertEquals(1, response.getMissingSkills().size());
        assertEquals("Docker", response.getMissingSkills().get(0));
    }

    @Test
    @DisplayName("getAnalysisById should throw exception when not found")
    void testGetAnalysisByIdNotFound() {
        // Setup
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(ResourceNotFoundException.class, () -> {
            resumeAnalysisService.getAnalysisById(999L);
        });
    }
}

