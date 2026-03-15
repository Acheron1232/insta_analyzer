package com.acheron.inst_bot.repository;

import com.acheron.inst_bot.model.InstagramProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InstagramProfileRepository extends JpaRepository<InstagramProfile, Long> {

    Optional<InstagramProfile> findByUsername(String username);
    Optional<InstagramProfile> findByUsernameIgnoreCase(String username);

    @Query("""
            SELECT p FROM InstagramProfile p
            WHERE p.dedupeKey = :dedupeKey
              AND LOWER(p.username) <> LOWER(:username)
            ORDER BY p.id ASC
            """)
    java.util.List<InstagramProfile> findPotentialDuplicates(
            @Param("dedupeKey") String dedupeKey,
            @Param("username") String username
    );

    @Query("SELECT p FROM InstagramProfile p JOIN p.analysisResult a ORDER BY COALESCE(a.rankPosition, 2147483647) ASC")
    Page<InstagramProfile> findAllRanked(Pageable pageable);

    @Query("SELECT p FROM InstagramProfile p JOIN p.analysisResult a ORDER BY COALESCE(a.rankPosition, 2147483647) ASC")
    java.util.List<InstagramProfile> findAllRanked();

    @Query("""
            SELECT p FROM InstagramProfile p JOIN p.analysisResult a
            WHERE a.overallScore >= :minScore
              AND a.smallBusiness = true
              AND a.ukraineBased = true
              AND a.lacksWebsite = true
              AND a.lacksCrm = true
              AND a.lacksTelegramBot = true
              AND a.active = true
            ORDER BY COALESCE(a.rankPosition, 2147483647) ASC
            """)
    java.util.List<InstagramProfile> findQualifiedLeads(@Param("minScore") int minScore);

    @Query("""
            SELECT COUNT(p) FROM InstagramProfile p JOIN p.analysisResult a
            WHERE a.overallScore >= :minScore
              AND a.smallBusiness = true
              AND a.ukraineBased = true
              AND a.lacksWebsite = true
              AND a.lacksCrm = true
              AND a.lacksTelegramBot = true
              AND a.active = true
            """)
    long countQualifiedLeads(@Param("minScore") int minScore);
}
