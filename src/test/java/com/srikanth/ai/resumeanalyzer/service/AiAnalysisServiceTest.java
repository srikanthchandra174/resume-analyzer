package com.srikanth.ai.resumeanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srikanth.ai.resumeanalyzer.exception.AnalysisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AiAnalysisService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiAnalysisService Tests")
class AiAnalysisServiceTest {

    private AiAnalysisService aiAnalysisService;

    @Mock
    private ChatClient chatClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aiAnalysisService = new AiAnalysisService(chatClient, objectMapper);
    }

    @Test
    @DisplayName("analyzeResumeAgainstJobDescription should parse well-formed JSON response")
    void testAnalyzeWithWellFormedJson() {
        String jsonResponse = """
            {
              "matchScore": 85,
              "missingSkills": ["Kubernetes", "Docker"],
              "suggestedImprovements": ["Add cloud skills"],
              "interviewQuestions": ["What is microservices?"],
              "summary": "Good fit"
            }
            """;

        mockChatClientResponse(jsonResponse);

        Map<String, Object> result = aiAnalysisService.analyzeResumeAgainstJobDescription(
            "test resume",
            "test job description"
        );

        assertNotNull(result);
        assertEquals(85.0, ((Number) result.get("matchScore")).doubleValue());
        assertEquals("Good fit", result.get("summary"));
    }

    @Test
    @DisplayName("analyzeResumeAgainstJobDescription should extract JSON from text with extra content")
    void testAnalyzeWithJsonSurroundedByText() {
        String responseWithExtra = """
            The AI analysis is:
            {
              "matchScore": 75,
              "missingSkills": ["GraphQL"],
              "suggestedImprovements": [],
              "interviewQuestions": [],
              "summary": "Partial match"
            }
            That's the analysis.
            """;

        mockChatClientResponse(responseWithExtra);

        Map<String, Object> result = aiAnalysisService.analyzeResumeAgainstJobDescription(
            "test resume",
            "test job description"
        );

        assertNotNull(result);
        assertEquals(75.0, ((Number) result.get("matchScore")).doubleValue());
        assertEquals("Partial match", result.get("summary"));
    }

    @Test
    @DisplayName("analyzeResumeAgainstJobDescription should provide defaults for missing optional fields")
    void testAnalyzeWithMissingOptionalFields() {
        String minimalJson = """
            {
              "matchScore": 80
            }
            """;

        mockChatClientResponse(minimalJson);

        Map<String, Object> result = aiAnalysisService.analyzeResumeAgainstJobDescription(
            "test resume",
            "test job description"
        );

        assertNotNull(result);
        assertEquals(80.0, ((Number) result.get("matchScore")).doubleValue());

        @SuppressWarnings("unchecked")
        List<String> missingSkills = (List<String>) result.get("missingSkills");
        assertNotNull(missingSkills);
        assertTrue(missingSkills.isEmpty());

        @SuppressWarnings("unchecked")
        List<String> improvements = (List<String>) result.get("suggestedImprovements");
        assertNotNull(improvements);
        assertTrue(improvements.isEmpty());

        @SuppressWarnings("unchecked")
        List<String> questions = (List<String>) result.get("interviewQuestions");
        assertNotNull(questions);
        assertTrue(questions.isEmpty());

        assertEquals("Analysis completed successfully", result.get("summary"));
    }

    @Test
    @DisplayName("analyzeResumeAgainstJobDescription should normalize integer matchScore to Double")
    void testAnalyzeNormalizeScore() {
        String jsonWithIntScore = """
            {
              "matchScore": 90,
              "missingSkills": [],
              "suggestedImprovements": [],
              "interviewQuestions": [],
              "summary": "Excellent"
            }
            """;

        mockChatClientResponse(jsonWithIntScore);

        Map<String, Object> result = aiAnalysisService.analyzeResumeAgainstJobDescription(
            "test resume",
            "test job description"
        );

        Object scoreObj = result.get("matchScore");
        assertTrue(scoreObj instanceof Double, "matchScore should be Double");
        assertEquals(90.0, (Double) scoreObj);
    }

    @Test
    @DisplayName("analyzeResumeAgainstJobDescription should throw exception when matchScore is missing")
    void testAnalyzeMissingMatchScore() {
        String jsonMissingScore = """
            {
              "missingSkills": [],
              "suggestedImprovements": [],
              "interviewQuestions": [],
              "summary": "No score"
            }
            """;

        mockChatClientResponse(jsonMissingScore);

        assertThrows(AnalysisException.class, () -> {
            aiAnalysisService.analyzeResumeAgainstJobDescription(
                "test resume",
                "test job description"
            );
        });
    }

    @Test
    @DisplayName("analyzeResumeAgainstJobDescription should throw exception for unparseable text")
    void testAnalyzeUnparseableText() {
        String plainText = "This is just plain text, not JSON at all";

        mockChatClientResponse(plainText);

        assertThrows(AnalysisException.class, () -> {
            aiAnalysisService.analyzeResumeAgainstJobDescription(
                "test resume",
                "test job description"
            );
        });
    }

    @Test
    @DisplayName("analyzeResumeAgainstJobDescription should wrap ChatClient exceptions")
    void testAnalyzeChatClientException() {
        when(chatClient.prompt(anyString())).thenThrow(new RuntimeException("Chat service error"));

        assertThrows(AnalysisException.class, () -> {
            aiAnalysisService.analyzeResumeAgainstJobDescription(
                "test resume",
                "test job description"
            );
        });
    }

    /**
     * Helper method to mock ChatClient response using RETURNS_DEEP_STUBS
     */
    private void mockChatClientResponse(String response) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class,
            org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call().content()).thenReturn(response);
    }
}


