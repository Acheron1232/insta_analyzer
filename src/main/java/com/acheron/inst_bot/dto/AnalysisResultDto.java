package com.acheron.inst_bot.dto;

import com.acheron.inst_bot.model.AnalysisResult;
import lombok.Builder;

@Builder
public record AnalysisResultDto(
        Integer rankPosition,
        int overallScore,
        boolean smallBusiness,
        boolean ukraineBased,
        boolean lacksWebsite,
        boolean lacksCrm,
        boolean lacksTelegramBot,
        boolean sellsViaDms,
        boolean active,
        String detectedCategory,
        String reasoning,
        String aiProvider,
        String analyzedAt
) {
    public static AnalysisResultDto from(AnalysisResult a) {
        return AnalysisResultDto.builder()
                .rankPosition(a.getRankPosition())
                .overallScore(a.getOverallScore())
                .smallBusiness(a.isSmallBusiness())
                .ukraineBased(a.isUkraineBased())
                .lacksWebsite(a.isLacksWebsite())
                .lacksCrm(a.isLacksCrm())
                .lacksTelegramBot(a.isLacksTelegramBot())
                .sellsViaDms(a.isSellsViaDms())
                .active(a.isActive())
                .detectedCategory(a.getDetectedCategory())
                .reasoning(a.getReasoning())
                .aiProvider(a.getAiProvider() != null ? a.getAiProvider().name() : null)
                .analyzedAt(a.getAnalyzedAt() != null ? a.getAnalyzedAt().toString() : null)
                .build();
    }
}
