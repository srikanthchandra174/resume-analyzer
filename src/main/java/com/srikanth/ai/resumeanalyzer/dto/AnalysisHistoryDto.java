package com.srikanth.ai.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for analysis history retrieval
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisHistoryDto {

    private Long id;

    private String jobTitle;

    private Double matchScore;

    private Long createdAt;

    private String fileName;
}

