package com.srikanth.ai.resumeanalyzer.repository;

import com.srikanth.ai.resumeanalyzer.entity.ResumeAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ResumeAnalysis entity
 */
@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    /**
     * Find analysis by file name
     */
    Optional<ResumeAnalysis> findByFileName(String fileName);

    /**
     * Find all analyses with pagination
     */
    Page<ResumeAnalysis> findAll(Pageable pageable);

    /**
     * Find analyses by match score range
     */
    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.matchScore >= :minScore AND ra.matchScore <= :maxScore")
    List<ResumeAnalysis> findByMatchScoreRange(
        @Param("minScore") Double minScore,
        @Param("maxScore") Double maxScore
    );

    /**
     * Find analyses created after a specific date
     */
    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.createdAt >= :startDate ORDER BY ra.createdAt DESC")
    List<ResumeAnalysis> findRecentAnalyses(@Param("startDate") LocalDateTime startDate);

    /**
     * Find analyses by embedding ID
     */
    Optional<ResumeAnalysis> findByEmbeddingId(String embeddingId);
}

