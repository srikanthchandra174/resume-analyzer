package com.srikanth.ai.resumeanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for storing resume analysis results
 */
@Entity
@Table(name = "resume_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumeText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "match_score", nullable = false)
    private Double matchScore;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "missing_skills", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "skill")
    private List<String> missingSkills;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "suggested_improvements", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "improvement")
    private List<String> suggestedImprovements;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interview_questions", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "question")
    private List<String> interviewQuestions;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "embedding_id")
    private String embeddingId;
}

