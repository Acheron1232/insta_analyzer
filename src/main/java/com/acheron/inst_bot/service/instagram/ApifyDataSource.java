package com.acheron.inst_bot.service.instagram;

import com.acheron.inst_bot.config.InstagramSourceConfig;
import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.model.enums.DataSource;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApifyDataSource implements InstagramDataSource {

    private final RestClient restClient;
    private final InstagramSourceConfig config;
    private final JsonMapper objectMapper;

    @Override
    public DataSource getType() {
        return DataSource.APIFY;
    }

    @Override
    public List<InstagramProfile> searchProfiles(String query, int limit) {
        String url = config.apify().baseUrl()
                + "/acts/apify~instagram-hashtag-scraper/run-sync-get-dataset-items?token="
                + config.apify().token();

        String body;
        try {
            String hashtag = query.startsWith("#") ? query.substring(1) : query;
            body = objectMapper.writeValueAsString(Map.of(
                    "hashtags", List.of(hashtag),
                    "resultsLimit", limit
            ));
        } catch (Exception e) {
            log.error("Failed to serialize Apify request", e);
            return List.of();
        }

        try {
            String response = restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseHashtagResults(response);
        } catch (Exception e) {
            log.error("Apify hashtag search failed for query: {}", query, e);
            return List.of();
        }
    }

    @Override
    public InstagramProfile fetchProfile(String username) {
        String url = config.apify().baseUrl()
                + "/acts/apify~instagram-profile-scraper/run-sync-get-dataset-items?token="
                + config.apify().token();

        String body;
        try {
            body = objectMapper.writeValueAsString(Map.of(
                    "usernames", List.of(username)
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Apify request", e);
        }

        String response = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.isArray() && !root.isEmpty()) {
                return mapApifyProfile(root.get(0));
            }
        } catch (Exception e) {
            log.error("Failed to parse Apify profile response for: {}", username, e);
        }
        throw new RuntimeException("Profile not found via Apify: " + username);
    }

    private List<InstagramProfile> parseHashtagResults(String response) {
        List<InstagramProfile> profiles = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    JsonNode ownerNode = node.path("ownerUsername");
                    if (!ownerNode.isMissingNode()) {
                        profiles.add(InstagramProfile.builder()
                                .username(ownerNode.asText())
                                .dataSource(DataSource.APIFY)
                                .fetchedAt(LocalDateTime.now())
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Apify hashtag results", e);
        }
        return profiles;
    }

    private InstagramProfile mapApifyProfile(JsonNode node) {
        return InstagramProfile.builder()
                .username(node.path("username").asText())
                .fullName(node.path("fullName").asText(""))
                .biography(node.path("biography").asText(""))
                .externalUrl(node.path("externalUrl").asText(null))
                .profilePicUrl(node.path("profilePicUrl").asText(null))
                .followerCount(node.path("followersCount").asInt(0))
                .followingCount(node.path("followsCount").asInt(0))
                .mediaCount(node.path("postsCount").asInt(0))
                .businessAccount(node.path("isBusinessAccount").asBoolean(false))
                .privateAccount(node.path("private").asBoolean(false))
                .verified(node.path("verified").asBoolean(false))
                .category(node.path("businessCategoryName").asText(null))
                .dataSource(DataSource.APIFY)
                .fetchedAt(LocalDateTime.now())
                .build();
    }
}
