package com.acheron.inst_bot.dto;

import lombok.Builder;

@Builder
public record DashboardStats(
        long totalProfiles,
        long totalLeads,
        double avgScore,
        long totalScans,
        long runningScans
) {}
