package com.srikanth.ai.resumeanalyzer.service;

import com.srikanth.ai.resumeanalyzer.dto.AnalysisHistoryDto;
import com.srikanth.ai.resumeanalyzer.dto.ResumeAnalysisResponse;
import com.srikanth.ai.resumeanalyzer.entity.ResumeAnalysis;
import com.srikanth.ai.resumeanalyzer.exception.ResourceNotFoundException;
import com.srikanth.ai.resumeanalyzer.repository.ResumeAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main business service for resume analysis orchestration
 */
@Slf4j
@Service
public class ResumeAnalysisService {

    private final PdfExtractionService pdfExtractionService;
    private final AiAnalysisService aiAnalysisService;
    private final CacheService cacheService;
    private final ResumeAnalysisRepository repository;

    public ResumeAnalysisService(
        PdfExtractionService pdfExtractionService,
        AiAnalysisService aiAnalysisService,
        CacheService cacheService,
        ResumeAnalysisRepository repository
    ) {
        this.pdfExtractionService = pdfExtractionService;
        this.aiAnalysisService = aiAnalysisService;
        this.cacheService = cacheService;
        this.repository = repository;
    }

    /**
     * Analyzes a resume against a job description
     *
     * @param resumeFile The resume PDF file
     * @param jobDescription The job description
     * @return Analysis response with results
     */
    @Transactional
    public ResumeAnalysisResponse analyzeResume(
        MultipartFile resumeFile,
        String jobDescription
    ) {
        log.info("Starting resume analysis for file: {}", resumeFile.getOriginalFilename());

        try {
            // Step 1: Extract text from PDF
            String resumeText = pdfExtractionService.extractTextFromPdf(resumeFile);

            // Step 2: Check cache
            String cacheKey = cacheService.generateCacheKey(resumeText, jobDescription);
            Map<String, Object> cachedResult = cacheService.getCachedAnalysis(cacheKey);

            if (cachedResult != null) {
                log.info("Using cached analysis result");
                return buildResponseFromCache(cachedResult);
            }

            // Step 3: Perform AI analysis
            Map<String, Object> analysisResult = aiAnalysisService.analyzeResumeAgainstJobDescription(
                resumeText,
                jobDescription
            );

            // Step 4: Save to database
            ResumeAnalysis analysis = saveAnalysisToDatabase(
                resumeFile,
                resumeText,
                jobDescription,
                analysisResult
            );

            // Step 5: Cache the result
            cacheService.cacheAnalysis(cacheKey, analysisResult);

            // Step 6: Build response
            return buildResponse(analysis, analysisResult);

        } catch (Exception e) {
            log.error("Error during resume analysis: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves analysis by ID
     *
     * @param id The analysis ID
     * @return Analysis response
     */
    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getAnalysisById(Long id) {
        ResumeAnalysis analysis = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("Analysis with ID %d not found", id)
            ));

        return buildResponseFromEntity(analysis);
    }

    /**
     * Retrieves analysis history with pagination
     *
     * @param pageable Pagination info
     * @return Page of analysis history
     */
    @Transactional(readOnly = true)
    public Page<AnalysisHistoryDto> getAnalysisHistory(Pageable pageable) {
        return repository.findAll(pageable)
            .map(this::convertToHistoryDto);
    }

    /**
     * Gets recent analyses
     *
     * @param hoursBack Number of hours to look back
     * @return List of recent analyses
     */
    @Transactional(readOnly = true)
    public List<AnalysisHistoryDto> getRecentAnalyses(int hoursBack) {
        LocalDateTime startDate = LocalDateTime.now().minusHours(hoursBack);
        return repository.findRecentAnalyses(startDate)
            .stream()
            .map(this::convertToHistoryDto)
            .collect(Collectors.toList());
    }

    /**
     * Saves analysis to database
     */
    private ResumeAnalysis saveAnalysisToDatabase(
        MultipartFile resumeFile,
        String resumeText,
        String jobDescription,
        Map<String, Object> analysisResult
    ) {
        @SuppressWarnings("unchecked")
        List<String> missingSkills = (List<String>) analysisResult.getOrDefault("missingSkills", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> improvements = (List<String>) analysisResult.getOrDefault("suggestedImprovements", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> questions = (List<String>) analysisResult.getOrDefault("interviewQuestions", new ArrayList<>());

        Double matchScore = ((Number) analysisResult.get("matchScore")).doubleValue();
        String summary = (String) analysisResult.getOrDefault("summary", "");

        ResumeAnalysis analysis = ResumeAnalysis.builder()
            .fileName(resumeFile.getOriginalFilename())
            .resumeText(resumeText)
            .jobDescription(jobDescription)
            .matchScore(matchScore)
            .missingSkills(missingSkills)
            .suggestedImprovements(improvements)
            .interviewQuestions(questions)
            .summary(summary)
            .build();

        ResumeAnalysis savedAnalysis = repository.save(analysis);
        log.info("Saved analysis to database with ID: {}", savedAnalysis.getId());
        return savedAnalysis;
    }

    /**
     * Builds response from cached result
     */
    private ResumeAnalysisResponse buildResponseFromCache(Map<String, Object> cachedResult) {
        @SuppressWarnings("unchecked")
        List<String> missingSkills = (List<String>) cachedResult.getOrDefault("missingSkills", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> improvements = (List<String>) cachedResult.getOrDefault("suggestedImprovements", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> questions = (List<String>) cachedResult.getOrDefault("interviewQuestions", new ArrayList<>());

        Double matchScore = ((Number) cachedResult.get("matchScore")).doubleValue();
        String summary = (String) cachedResult.getOrDefault("summary", "");

        return ResumeAnalysisResponse.builder()
            .matchScore(matchScore)
            .missingSkills(missingSkills)
            .suggestedImprovements(improvements)
            .interviewQuestions(questions)
            .summary(summary)
            .build();
    }

    /**
     * Builds response from analysis entity
     */
    private ResumeAnalysisResponse buildResponseFromEntity(ResumeAnalysis analysis) {
        return ResumeAnalysisResponse.builder()
            .analysisId(analysis.getId())
            .matchScore(analysis.getMatchScore())
            .missingSkills(analysis.getMissingSkills())
            .suggestedImprovements(analysis.getSuggestedImprovements())
            .interviewQuestions(analysis.getInterviewQuestions())
            .summary(analysis.getSummary())
            .build();
    }

    /**
     * Builds response from entity and raw analysis result
     */
    private ResumeAnalysisResponse buildResponse(
        ResumeAnalysis analysis,
        Map<String, Object> analysisResult
    ) {
        return ResumeAnalysisResponse.builder()
            .analysisId(analysis.getId())
            .matchScore(analysis.getMatchScore())
            .missingSkills(analysis.getMissingSkills())
            .suggestedImprovements(analysis.getSuggestedImprovements())
            .interviewQuestions(analysis.getInterviewQuestions())
            .summary(analysis.getSummary())
            .build();
    }

    /**
     * Converts entity to history DTO
     */
    private AnalysisHistoryDto convertToHistoryDto(ResumeAnalysis analysis) {
        return AnalysisHistoryDto.builder()
            .id(analysis.getId())
            .jobTitle("Job Analysis")
            .matchScore(analysis.getMatchScore())
            .createdAt(analysis.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
            .fileName(analysis.getFileName())
            .build();
    }
}

