package com.acheron.inst_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "instagram")
public record InstagramSourceConfig(
        Instagram4jConfig instagram4j,
        ApifyConfig apify,
        RapidApiConfig rapidapi
) {
    public record Instagram4jConfig(String username, String password) {}
    public record ApifyConfig(String token, String baseUrl) {}
    public record RapidApiConfig(String key, String host) {}
}
