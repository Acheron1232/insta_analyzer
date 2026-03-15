package com.acheron.inst_bot.service.ai;

import com.acheron.inst_bot.config.AiProviderConfig;
import com.acheron.inst_bot.model.AnalysisResult;
import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.model.enums.AiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaAnalyzer implements AiAnalyzer {

    private final RestClient restClient;
    private final AiProviderConfig config;
    private final JsonMapper objectMapper;

    @Override
    public AiProvider getType() {
        return AiProvider.OLLAMA;
    }

    @Override
    public Map<String, AnalysisResult> analyzeBatch(List<InstagramProfile> profiles) {
        String prompt = AnalysisPrompt.buildBatch(profiles);

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", config.ollama().model(),
                    "prompt", prompt,
                    "format", "json",
                    "stream", false
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Ollama request", e);
        }

        String url = config.ollama().baseUrl() + "/api/generate";

        log.info("Sending batch of {} profiles to Ollama for analysis", profiles.size());

        String response = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return parseResponse(response, profiles);
    }

    private Map<String, AnalysisResult> parseResponse(String response, List<InstagramProfile> profiles) {
        Map<String, AnalysisResult> results = new HashMap<>();
        Map<String, InstagramProfile> profileMap = new HashMap<>();
        for (InstagramProfile p : profiles) {
            profileMap.put(p.getUsername(), p);
        }

        try {
            JsonNode root = objectMapper.readTree(response);
            String aiResponse = root.path("response").asText();
            JsonNode parsed = objectMapper.readTree(aiResponse);

            // Ollama with format=json might wrap array in an object
            JsonNode resultsArray;
            if (parsed.isArray()) {
                resultsArray = parsed;
            } else if (parsed.has("results")) {
                resultsArray = parsed.get("results");
            } else if (parsed.has("profiles")) {
                resultsArray = parsed.get("profiles");
            } else {
                // Single result wrapped in object — treat as array of 1
                resultsArray = objectMapper.createArrayNode().add(parsed);
            }

            for (JsonNode item : resultsArray) {
                String username = item.path("username").asText();
                InstagramProfile profile = profileMap.get(username);
                if (profile == null) {
                    log.warn("AI returned result for unknown username: {}", username);
                    continue;
                }

                results.put(username, AnalysisResult.builder()
                        .profile(profile)
                        .overallScore(item.path("overallScore").asInt(0))
                        .smallBusiness(item.path("isSmallBusiness").asBoolean(false))
                        .ukraineBased(item.path("ukraineBased").asBoolean(item.path("isUkraineBased").asBoolean(false)))
                        .lacksWebsite(item.path("lacksWebsite").asBoolean(false))
                        .lacksCrm(item.path("lacksCrm").asBoolean(true))
                        .lacksTelegramBot(item.path("lacksTelegramBot").asBoolean(true))
                        .sellsViaDms(item.path("sellsViaDms").asBoolean(false))
                        .active(item.path("isActive").asBoolean(false))
                        .detectedCategory(item.path("detectedCategory").asText("unknown"))
                        .reasoning(item.path("reasoning").asText(""))
                        .aiProvider(AiProvider.OLLAMA)
                        .analyzedAt(LocalDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to parse Ollama batch response", e);
            throw new RuntimeException("Ollama batch analysis failed", e);
        }

        log.info("Ollama returned {} results out of {} profiles", results.size(), profiles.size());
        return results;
    }
}
