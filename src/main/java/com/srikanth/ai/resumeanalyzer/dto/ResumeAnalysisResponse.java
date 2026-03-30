package com.srikanth.ai.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for resume analysis response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private Long analysisId;

    private Double matchScore;

    private List<String> missingSkills;

    private List<String> suggestedImprovements;

    private List<String> interviewQuestions;

    private String summary;
}

