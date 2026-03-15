package com.acheron.inst_bot.repository;

import com.acheron.inst_bot.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    long countByOverallScoreGreaterThanEqual(int minScore);

    /**
     * Recomputes global rank_position for all analyzed profiles.
     * Rank 1 = highest overall_score. Uses ROW_NUMBER() supported by SQLite 3.25+.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE analysis_results
            SET rank_position = (
                SELECT rn FROM (
                    SELECT id, ROW_NUMBER() OVER (ORDER BY overall_score DESC) AS rn
                    FROM analysis_results
                ) ranked
                WHERE ranked.id = analysis_results.id
            )
            """, nativeQuery = true)
    void rerank();
}
