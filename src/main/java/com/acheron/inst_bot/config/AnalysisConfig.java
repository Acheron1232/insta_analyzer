package com.acheron.inst_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analysis")
public record AnalysisConfig(
        int minFollowers,
        int maxFollowers,
        int leadScoreThreshold,
        long requestDelayMs,
        int aiBatchSize
) {}
