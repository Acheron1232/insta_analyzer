package com.acheron.inst_bot.service.ai;

import com.acheron.inst_bot.model.AnalysisResult;
import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.model.enums.AiProvider;

import java.util.List;
import java.util.Map;

public interface AiAnalyzer {

    AiProvider getType();

    /**
     * Analyze a batch of profiles in a single AI request.
     * Returns a map of username → AnalysisResult.
     */
    Map<String, AnalysisResult> analyzeBatch(List<InstagramProfile> profiles);
}
