package com.acheron.inst_bot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

@Configuration
@EnableAsync
@EnableConfigurationProperties({
        InstagramSourceConfig.class,
        AiProviderConfig.class,
        AnalysisConfig.class
})
public class AppConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
