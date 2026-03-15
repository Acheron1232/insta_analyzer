package com.acheron.inst_bot.service;

import com.acheron.inst_bot.model.AnalysisResult;
import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.model.enums.AiProvider;
import com.acheron.inst_bot.model.enums.DataSource;
import com.acheron.inst_bot.repository.AnalysisResultRepository;
import com.acheron.inst_bot.repository.InstagramProfileRepository;
import com.acheron.inst_bot.service.ai.AiAnalyzer;
import com.acheron.inst_bot.service.ai.AiAnalyzerFactory;
import com.acheron.inst_bot.service.instagram.InstagramDataSourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileAnalysisService {

    private final InstagramDataSourceFactory dataSourceFactory;
    private final AiAnalyzerFactory aiAnalyzerFactory;
    private final InstagramProfileRepository profileRepo;
    private final AnalysisResultRepository analysisResultRepo;
    private final ProfileIdentityService identityService;

    @Transactional
    public InstagramProfile analyzeProfile(String username, DataSource source, AiProvider aiProvider) {
        String normalizedUsername = identityService.normalizeUsername(username);
        Optional<InstagramProfile> existing = profileRepo.findByUsernameIgnoreCase(normalizedUsername);
        if (existing.isPresent() && existing.get().getAnalysisResult() != null) {
            InstagramProfile p = existing.get();
            if (p.getAnalysisResult().getAnalyzedAt().isAfter(LocalDateTime.now().minusHours(24))) {
                log.info("@{} already analyzed recently, skipping", normalizedUsername);
                return p;
            }
        }

        var dataSource = dataSourceFactory.get(source);
        InstagramProfile profile = dataSource.fetchProfile(normalizedUsername);
        InstagramProfile saved = saveProfile(profile, existing.orElse(null));

        AiAnalyzer analyzer = aiAnalyzerFactory.get(aiProvider);
        Map<String, AnalysisResult> results = analyzer.analyzeBatch(List.of(saved));

        AnalysisResult result = results.get(saved.getUsername());
        if (result != null) {
            if (saved.getAnalysisResult() != null) {
                result.setId(saved.getAnalysisResult().getId());
            }
            result.setProfile(saved);
            saved.setAnalysisResult(result);
            profileRepo.save(saved);
        }

        analysisResultRepo.rerank();
        log.info("Global rerank complete after single-profile analysis of @{}", saved.getUsername());
        return saved;
    }

    /**
     * Analyzes a batch of already-fetched profiles with one AI call, then reraks globally.
     */
    @Transactional
    public List<InstagramProfile> analyzeBatch(List<InstagramProfile> profiles, AiProvider aiProvider) {
        if (profiles.isEmpty()) return List.of();

        log.info("Sending {} profiles to AI for scoring", profiles.size());

        AiAnalyzer analyzer = aiAnalyzerFactory.get(aiProvider);
        Map<String, AnalysisResult> results = analyzer.analyzeBatch(profiles);

        for (InstagramProfile profile : profiles) {
            AnalysisResult result = results.get(profile.getUsername());
            if (result != null) {
                if (profile.getAnalysisResult() != null) {
                    result.setId(profile.getAnalysisResult().getId());
                }
                result.setProfile(profile);
                profile.setAnalysisResult(result);
                profileRepo.save(profile);

                log.info("SCORED @{} | score={} | smallBiz={} ua={} lacksWeb={} lacksCrm={} lacksTg={} sellsDMs={} active={} | {}",
                        profile.getUsername(),
                        result.getOverallScore(),
                        result.isSmallBusiness(),
                        result.isUkraineBased(),
                        result.isLacksWebsite(),
                        result.isLacksCrm(),
                        result.isLacksTelegramBot(),
                        result.isSellsViaDms(),
                        result.isActive(),
                        result.getReasoning() != null
                                ? result.getReasoning().substring(0, Math.min(120, result.getReasoning().length()))
                                : "no reasoning");
            } else {
                log.warn("AI returned no result for @{}", profile.getUsername());
            }
        }

        return profiles;
    }

    /**
     * Recomputes rank_position for ALL profiles in DB (rank 1 = best candidate).
     * Call this once after all batches of a scan are saved.
     */
    @Transactional
    public void rerank() {
        analysisResultRepo.rerank();
        log.info("Global rerank complete — rank 1 is now the best candidate");
    }

    @Transactional
    public InstagramProfile fetchAndSaveProfile(String username, DataSource source) {
        String normalizedUsername = identityService.normalizeUsername(username);
        Optional<InstagramProfile> existing = profileRepo.findByUsernameIgnoreCase(normalizedUsername);

        if (existing.isPresent() && existing.get().getFetchedAt() != null
                && existing.get().getFetchedAt().isAfter(LocalDateTime.now().minusHours(24))) {
            log.debug("@{} fetched recently, using cached profile", normalizedUsername);
            return existing.get();
        }

        var dataSource = dataSourceFactory.get(source);
        InstagramProfile profile = dataSource.fetchProfile(normalizedUsername);
        return saveProfile(profile, existing.orElse(null));
    }

    public InstagramProfile saveProfile(InstagramProfile fetched, InstagramProfile existing) {
        ProfileIdentityService.Identity identity = identityService.buildIdentity(fetched);
        if (identity.normalizedUsername().isBlank()) {
            throw new IllegalArgumentException("Fetched profile has blank username");
        }

        fetched.setUsername(identity.normalizedUsername());
        fetched.setWebsiteDomain(identity.websiteDomain());
        fetched.setContactPhone(identity.contactPhone());
        fetched.setTelegramHandle(identity.telegramHandle());
        fetched.setDedupeKey(identity.dedupeKey());

        if (existing == null && identity.dedupeKey() != null && !identity.dedupeKey().isBlank()) {
            List<InstagramProfile> duplicates = profileRepo.findPotentialDuplicates(
                    identity.dedupeKey(),
                    fetched.getUsername()
            );
            if (!duplicates.isEmpty()) {
                existing = duplicates.get(0);
                log.info("Deduplicated @{} into existing @{} using key={}",
                        fetched.getUsername(), existing.getUsername(), identity.dedupeKey());
            }
        }

        if (existing != null) {
            existing.setFullName(fetched.getFullName());
            existing.setBiography(fetched.getBiography());
            existing.setExternalUrl(fetched.getExternalUrl());
            existing.setProfilePicUrl(fetched.getProfilePicUrl());
            existing.setFollowerCount(fetched.getFollowerCount());
            existing.setFollowingCount(fetched.getFollowingCount());
            existing.setMediaCount(fetched.getMediaCount());
            existing.setBusinessAccount(fetched.isBusinessAccount());
            existing.setPrivateAccount(fetched.isPrivateAccount());
            existing.setVerified(fetched.isVerified());
            existing.setCategory(fetched.getCategory());
            existing.setWebsiteDomain(fetched.getWebsiteDomain());
            existing.setContactPhone(fetched.getContactPhone());
            existing.setTelegramHandle(fetched.getTelegramHandle());
            existing.setDedupeKey(fetched.getDedupeKey());
            existing.setDataSource(fetched.getDataSource());
            existing.setFetchedAt(fetched.getFetchedAt());
            existing.setLastPostAt(fetched.getLastPostAt());
            return profileRepo.save(existing);
        }
        return profileRepo.save(fetched);
    }
}
