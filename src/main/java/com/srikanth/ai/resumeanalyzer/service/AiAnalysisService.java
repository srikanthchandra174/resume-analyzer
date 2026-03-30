package com.srikanth.ai.resumeanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srikanth.ai.resumeanalyzer.exception.AnalysisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Service for AI-powered resume analysis using Spring AI
 */
@Slf4j
@Service
public class AiAnalysisService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Analyzes resume against job description using AI
     *
     * @param resumeText The extracted resume text
     * @param jobDescription The job description
     * @return Map containing analysis results
     * @throws AnalysisException if analysis fails
     */
    public Map<String, Object> analyzeResumeAgainstJobDescription(
        String resumeText,
        String jobDescription
    ) {
        try {
            log.info("Starting AI analysis for resume against job description");

            String prompt = buildAnalysisPrompt(resumeText, jobDescription);
            String response = chatClient.prompt(prompt)
                .call()
                .content();

            log.info("Received AI response, parsing results");
            return parseAnalysisResponse(response);

        } catch (Exception e) {
            log.error("Error during AI analysis: {}", e.getMessage(), e);
            throw new AnalysisException("Failed to analyze resume: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the analysis prompt for the AI model
     *
     * @param resumeText The resume text
     * @param jobDescription The job description
     * @return The formatted prompt
     */
    private String buildAnalysisPrompt(String resumeText, String jobDescription) {
        return String.format("""
            You are an expert HR analyst and technical recruiter. Analyze the following resume against the job description.
            
            RESUME:
            %s
            
            JOB DESCRIPTION:
            %s
            
            Provide a detailed analysis in JSON format with the following structure:
            {
              "matchScore": <number between 0-100>,
              "missingSkills": [<list of skills from JD not in resume>],
              "suggestedImprovements": [<list of suggestions to improve resume>],
              "interviewQuestions": [<list of 3-5 interview questions based on resume and JD>],
              "summary": "<brief summary of how well resume matches JD>"
            }
            
            Only return the JSON object, no additional text.
            """,
            truncateText(resumeText, 2000),
            truncateText(jobDescription, 1000)
        );
    }

    /**
     * Parses the AI response into a structured map
     *
     * @param response The response from AI model
     * @return Parsed analysis result
     * @throws AnalysisException if parsing fails
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAnalysisResponse(String response) {
        try {
            // Extract JSON from response (in case there's extra text)
            String jsonString = extractJsonFromResponse(response);

            Map<String, Object> analysisResult = objectMapper.readValue(jsonString, Map.class);

            // Validate and normalize response
            validateAnalysisResult(analysisResult);

            return analysisResult;
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", response, e);
            throw new AnalysisException("Failed to parse analysis response: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts JSON from response (handles cases where model returns extra text)
     *
     * @param response The raw response
     * @return The JSON string
     */
    private String extractJsonFromResponse(String response) {
        int jsonStart = response.indexOf('{');
        int jsonEnd = response.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }

        return response;
    }

    /**
     * Validates the analysis result structure
     *
     * @param result The analysis result map
     * @throws AnalysisException if validation fails
     */
    @SuppressWarnings("unchecked")
    private void validateAnalysisResult(Map<String, Object> result) {
        if (!result.containsKey("matchScore")) {
            throw new AnalysisException("Response missing 'matchScore'");
        }

        if (!result.containsKey("missingSkills")) {
            result.put("missingSkills", new ArrayList<>());
        }

        if (!result.containsKey("suggestedImprovements")) {
            result.put("suggestedImprovements", new ArrayList<>());
        }

        if (!result.containsKey("interviewQuestions")) {
            result.put("interviewQuestions", new ArrayList<>());
        }

        if (!result.containsKey("summary")) {
            result.put("summary", "Analysis completed successfully");
        }

        // Ensure lists are properly typed
        Object score = result.get("matchScore");
        if (score instanceof Number) {
            result.put("matchScore", ((Number) score).doubleValue());
        }
    }

    /**
     * Truncates text to avoid token limits
     *
     * @param text The text to truncate
     * @param maxLength Maximum length
     * @return Truncated text
     */
    private String truncateText(String text, int maxLength) {
        if (text != null && text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }
}

