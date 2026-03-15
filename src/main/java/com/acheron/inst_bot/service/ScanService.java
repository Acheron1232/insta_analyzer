package com.acheron.inst_bot.service;

import com.acheron.inst_bot.config.AnalysisConfig;
import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.model.ScanJob;
import com.acheron.inst_bot.model.enums.ScanStatus;
import com.acheron.inst_bot.repository.ScanJobRepository;
import com.acheron.inst_bot.service.instagram.InstagramDataSource;
import com.acheron.inst_bot.service.instagram.InstagramDataSourceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanService {

    private final ProfileAnalysisService analysisService;
    private final ScanJobRepository scanJobRepo;
    private final InstagramDataSourceFactory dataSourceFactory;
    private final AnalysisConfig analysisConfig;
    private final UkraineQueryPlanner ukraineQueryPlanner;

    public ScanJob createScan(String query, com.acheron.inst_bot.model.enums.DataSource dataSource,
                              com.acheron.inst_bot.model.enums.AiProvider aiProvider) {
        ScanJob job = ScanJob.builder()
                .searchQuery(query)
                .dataSource(dataSource)
                .aiProvider(aiProvider)
                .status(ScanStatus.PENDING)
                .startedAt(LocalDateTime.now())
                .build();
        return scanJobRepo.save(job);
    }

    @Async
    public void executeScan(Long jobId, int limit) {
        ScanJob job = scanJobRepo.findById(jobId).orElseThrow();
        job.setStatus(ScanStatus.RUNNING);
        scanJobRepo.save(job);

        try {
            InstagramDataSource source = dataSourceFactory.get(job.getDataSource());
            List<InstagramProfile> searchResults = source.searchProfiles(job.getSearchQuery(), limit);
            List<String> uniqueUsernames = dedupeUsernames(searchResults);
            completeScan(job, uniqueUsernames);
        } catch (Exception e) {
            log.error("Scan job {} failed", jobId, e);
            job.setStatus(ScanStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
        }
        scanJobRepo.save(job);
    }

    @Async
    public void executeUkraineScan(Long jobId, int limit, List<String> niches, List<String> cities) {
        ScanJob job = scanJobRepo.findById(jobId).orElseThrow();
        job.setStatus(ScanStatus.RUNNING);
        scanJobRepo.save(job);

        try {
            InstagramDataSource source = dataSourceFactory.get(job.getDataSource());
            List<String> queries = ukraineQueryPlanner.buildQueries(niches, cities);
            int perQueryLimit = Math.max(10, (int) Math.ceil((double) limit / Math.max(1, queries.size() / 2.0)));

            log.info("Ukraine scan {}: generated {} seed queries, perQueryLimit={}",
                    jobId, queries.size(), perQueryLimit);

            Set<String> seen = new HashSet<>();
            List<String> usernames = new ArrayList<>();

            for (String query : queries) {
                if (usernames.size() >= limit * 3) {
                    break;
                }

                try {
                    List<InstagramProfile> results = source.searchProfiles(query, perQueryLimit);
                    for (InstagramProfile profile : results) {
                        String normalizedUsername = normalizeUsername(profile.getUsername());
                        if (!normalizedUsername.isBlank() && seen.add(normalizedUsername)) {
                            usernames.add(normalizedUsername);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Ukraine scan {}: query '{}' failed: {}", jobId, query, e.getMessage());
                }
            }

            int candidateCap = Math.max(limit, limit * 2);
            if (usernames.size() > candidateCap) {
                usernames = new ArrayList<>(usernames.subList(0, candidateCap));
            }

            completeScan(job, usernames);
        } catch (Exception e) {
            log.error("Ukraine scan {} failed", jobId, e);
            job.setStatus(ScanStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            scanJobRepo.save(job);
        }
    }

    public List<ScanJob> getAllScans() {
        return scanJobRepo.findAllByOrderByStartedAtDesc();
    }

    private void completeScan(ScanJob job, List<String> candidateUsernames) {
        long jobId = job.getId();
        job.setProfilesFound(candidateUsernames.size());
        scanJobRepo.save(job);

        log.info("Scan job {}: found {} unique profiles, fetching details...", jobId, candidateUsernames.size());

        List<InstagramProfile> fetchedProfiles = fetchProfiles(job, candidateUsernames);

        job.setProfilesAnalyzed(fetchedProfiles.size());
        scanJobRepo.save(job);

        log.info("Scan job {}: fetched {} unique profiles, sending to AI in batches...", jobId, fetchedProfiles.size());

        int leads = analyzeBatches(job, fetchedProfiles);
        analysisService.rerank();

        job.setLeadsFound(leads);
        job.setStatus(ScanStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        scanJobRepo.save(job);

        log.info("Scan job {} completed: {} profiles scored, {} qualified leads",
                jobId, fetchedProfiles.size(), leads);
    }

    private List<InstagramProfile> fetchProfiles(ScanJob job, List<String> usernames) {
        List<InstagramProfile> fetchedProfiles = new ArrayList<>();
        Set<String> uniqueCanonicalUsernames = new HashSet<>();

        for (String username : usernames) {
            try {
                InstagramProfile profile = analysisService.fetchAndSaveProfile(username, job.getDataSource());
                String canonical = normalizeUsername(profile.getUsername());

                if (!canonical.isBlank() && uniqueCanonicalUsernames.add(canonical)) {
                    fetchedProfiles.add(profile);
                }

                Thread.sleep(analysisConfig.requestDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Failed to fetch profile {}: {}", username, e.getMessage());
            }
        }

        return fetchedProfiles;
    }

    private int analyzeBatches(ScanJob job, List<InstagramProfile> fetchedProfiles) {
        int batchSize = analysisConfig.aiBatchSize();
        int leads = 0;

        for (int i = 0; i < fetchedProfiles.size(); i += batchSize) {
            int end = Math.min(i + batchSize, fetchedProfiles.size());
            List<InstagramProfile> batch = fetchedProfiles.subList(i, end);
            long jobId = job.getId();

            log.info("Scan job {}: analyzing batch {}-{} of {}", jobId, i + 1, end, fetchedProfiles.size());

            try {
                List<InstagramProfile> analyzed = analysisService.analyzeBatch(batch, job.getAiProvider());
                int batchLeads = 0;

                for (InstagramProfile profile : analyzed) {
                    if (isQualifiedLead(profile)) {
                        batchLeads++;
                        log.info("QUALIFIED LEAD @{} | score={}",
                                profile.getUsername(),
                                profile.getAnalysisResult().getOverallScore());
                    }
                }

                leads += batchLeads;
                log.info("Batch {}-{}: {} qualified leads", i + 1, end, batchLeads);
            } catch (Exception e) {
                log.error("Scan job {}: batch analysis failed for batch {}-{}: {}",
                        jobId, i + 1, end, e.getMessage(), e);
            }
        }

        return leads;
    }

    private boolean isQualifiedLead(InstagramProfile profile) {
        if (profile.getAnalysisResult() == null) {
            return false;
        }

        var result = profile.getAnalysisResult();
        return result.getOverallScore() >= analysisConfig.leadScoreThreshold()
                && result.isSmallBusiness()
                && result.isUkraineBased()
                && result.isLacksWebsite()
                && result.isLacksCrm()
                && result.isLacksTelegramBot()
                && result.isActive();
    }

    private List<String> dedupeUsernames(List<InstagramProfile> searchResults) {
        Set<String> seen = new HashSet<>();
        List<String> uniqueUsernames = new ArrayList<>();

        for (InstagramProfile profile : searchResults) {
            String username = normalizeUsername(profile.getUsername());
            if (!username.isBlank() && seen.add(username)) {
                uniqueUsernames.add(username);
            }
        }

        return uniqueUsernames;
    }

    private String normalizeUsername(String username) {
        if (username == null) return "";
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
