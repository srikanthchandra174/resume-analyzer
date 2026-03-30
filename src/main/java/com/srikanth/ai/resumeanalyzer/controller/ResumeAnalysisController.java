package com.srikanth.ai.resumeanalyzer.controller;

import com.srikanth.ai.resumeanalyzer.dto.AnalysisHistoryDto;
import com.srikanth.ai.resumeanalyzer.dto.ResumeAnalysisRequest;
import com.srikanth.ai.resumeanalyzer.dto.ResumeAnalysisResponse;
import com.srikanth.ai.resumeanalyzer.service.ResumeAnalysisService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for resume analysis endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/resume-analysis")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ResumeAnalysisController {

    private final ResumeAnalysisService analysisService;

    public ResumeAnalysisController(ResumeAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Analyzes a resume against a job description
     *
     * POST /api/v1/resume-analysis/analyze
     *
     * @param request The analysis request containing resume file and job description
     * @return Analysis response with results
     */
    @PostMapping("/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
        @ModelAttribute @Valid ResumeAnalysisRequest request
    ) {
        log.info("Received resume analysis request for file: {}", request.getResumeFile().getOriginalFilename());

        ResumeAnalysisResponse response = analysisService.analyzeResume(
            request.getResumeFile(),
            request.getJobDescription()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific analysis by ID
     *
     * GET /api/v1/resume-analysis/{id}
     *
     * @param id The analysis ID
     * @return The analysis response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResumeAnalysisResponse> getAnalysis(@PathVariable Long id) {
        log.info("Fetching analysis with ID: {}", id);

        ResumeAnalysisResponse response = analysisService.getAnalysisById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves analysis history with pagination
     *
     * GET /api/v1/resume-analysis/history?page=0&size=10&sort=createdAt,desc
     *
     * @param pageable Pagination parameters
     * @return Paginated analysis history
     */
    @GetMapping("/history")
    public ResponseEntity<Page<AnalysisHistoryDto>> getAnalysisHistory(
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        log.info("Fetching analysis history with pagination: page={}, size={}",
            pageable.getPageNumber(), pageable.getPageSize());

        Page<AnalysisHistoryDto> history = analysisService.getAnalysisHistory(pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * Retrieves recent analyses
     *
     * GET /api/v1/resume-analysis/recent?hours=24
     *
     * @param hours Number of hours to look back (default: 24)
     * @return List of recent analyses
     */
    @GetMapping("/recent")
    public ResponseEntity<List<AnalysisHistoryDto>> getRecentAnalyses(
        @RequestParam(defaultValue = "24") int hours
    ) {
        log.info("Fetching recent analyses from last {} hours", hours);

        List<AnalysisHistoryDto> recentAnalyses = analysisService.getRecentAnalyses(hours);
        return ResponseEntity.ok(recentAnalyses);
    }

    /**
     * Health check endpoint
     *
     * GET /api/v1/resume-analysis/health
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Resume Analyzer API is up and running!");
    }
}

