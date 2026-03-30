package com.srikanth.ai.resumeanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for resume analysis request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisRequest {

    @NotNull(message = "Resume file cannot be null")
    private MultipartFile resumeFile;

    @NotBlank(message = "Job description cannot be blank")
    private String jobDescription;
}

