package com.acheron.inst_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiProviderConfig(
        OllamaConfig ollama,

        GeminiConfig gemini
) {
    public record OllamaConfig(String baseUrl, String model) {}
    public record GeminiConfig(String apiKey, String model) {}
}
