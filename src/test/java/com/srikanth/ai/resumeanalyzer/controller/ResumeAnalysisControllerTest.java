package com.srikanth.ai.resumeanalyzer.controller;

import com.srikanth.ai.resumeanalyzer.dto.ResumeAnalysisResponse;
import com.srikanth.ai.resumeanalyzer.service.ResumeAnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeAnalysisController Tests")
class ResumeAnalysisControllerTest {

    @Mock
    private ResumeAnalysisService analysisService;

    @InjectMocks
    private ResumeAnalysisController controller;

    private ResumeAnalysisResponse createMockResponse() {
        return ResumeAnalysisResponse.builder()
            .analysisId(1L)
            .matchScore(85.0)
            .summary("Strong match")
            .missingSkills(Arrays.asList("Kubernetes", "Docker"))
            .suggestedImprovements(Arrays.asList("Add cloud skills"))
            .interviewQuestions(Arrays.asList("Tell me about microservices"))
            .build();
    }

    @Test
    @DisplayName("health endpoint should return 200 with running message")
    void testHealthCheck() {
        ResponseEntity<String> response = controller.health();
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("up and running"));
    }

    @Test
    @DisplayName("analyzeResume should return 200 with response")
    void testAnalyzeResume() {
        ResumeAnalysisResponse response = createMockResponse();
        MultipartFile pdfFile = new MockMultipartFile("resumeFile", "resume.pdf", "application/pdf", "PDF".getBytes());
        when(analysisService.analyzeResume(any(MultipartFile.class), anyString())).thenReturn(response);
        com.srikanth.ai.resumeanalyzer.dto.ResumeAnalysisRequest request =
            com.srikanth.ai.resumeanalyzer.dto.ResumeAnalysisRequest.builder()
                .resumeFile(pdfFile)
                .jobDescription("Senior Backend Engineer")
                .build();
        ResponseEntity<ResumeAnalysisResponse> result = controller.analyzeResume(request);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getAnalysisId());
        assertEquals(85.0, result.getBody().getMatchScore());
    }

    @Test
    @DisplayName("getAnalysis should return 200 with response")
    void testGetAnalysisById() {
        ResumeAnalysisResponse response = createMockResponse();
        when(analysisService.getAnalysisById(1L)).thenReturn(response);
        ResponseEntity<ResumeAnalysisResponse> result = controller.getAnalysis(1L);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getAnalysisId());
    }
}

