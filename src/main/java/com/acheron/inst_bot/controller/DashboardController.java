package com.acheron.inst_bot.controller;

import com.acheron.inst_bot.config.AnalysisConfig;
import com.acheron.inst_bot.dto.DashboardStats;
import com.acheron.inst_bot.model.enums.ScanStatus;
import com.acheron.inst_bot.repository.AnalysisResultRepository;
import com.acheron.inst_bot.repository.InstagramProfileRepository;
import com.acheron.inst_bot.repository.ScanJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final InstagramProfileRepository profileRepo;
    private final AnalysisResultRepository analysisRepo;
    private final ScanJobRepository scanJobRepo;
    private final AnalysisConfig analysisConfig;

    @GetMapping("/stats")
    public DashboardStats getStats() {
        long totalProfiles = profileRepo.count();
        long totalLeads = profileRepo.countQualifiedLeads(analysisConfig.leadScoreThreshold());
        double avgScore = analysisRepo.findAll().stream()
                .mapToInt(a -> a.getOverallScore())
                .average()
                .orElse(0.0);
        long totalScans = scanJobRepo.count();
        long runningScans = scanJobRepo.findByStatusOrderByStartedAtDesc(ScanStatus.RUNNING).size();

        return DashboardStats.builder()
                .totalProfiles(totalProfiles)
                .totalLeads(totalLeads)
                .avgScore(Math.round(avgScore * 10.0) / 10.0)
                .totalScans(totalScans)
                .runningScans(runningScans)
                .build();
    }
}
