package com.acheron.inst_bot.service.ai;

import com.acheron.inst_bot.model.enums.AiProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiAnalyzerFactory {

    private final List<AiAnalyzer> analyzers;

    public AiAnalyzer get(AiProvider type) {
        return analyzers.stream()
                .filter(a -> a.getType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AI provider: " + type));
    }
}
