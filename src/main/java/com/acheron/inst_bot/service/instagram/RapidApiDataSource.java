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

@Component
@RequiredArgsConstructor
@Slf4j
public class RapidApiDataSource implements InstagramDataSource {

    private final RestClient restClient;
    private final InstagramSourceConfig config;
    private final JsonMapper objectMapper;

    @Override
    public DataSource getType() {
        return DataSource.RAPIDAPI;
    }

    @Override
    public List<InstagramProfile> searchProfiles(String query, int limit) {
        String host = config.rapidapi().host();
        String url = "https://" + host + "/v1/hashtag?hashtag=" + query;

        try {
            String response = restClient.get()
                    .uri(url)
                    .header("X-RapidAPI-Key", config.rapidapi().key())
                    .header("X-RapidAPI-Host", host)
                    .retrieve()
                    .body(String.class);

            return parseSearchResults(response, limit);
        } catch (Exception e) {
            log.error("RapidAPI search failed for query: {}", query, e);
            return List.of();
        }
    }

    @Override
    public InstagramProfile fetchProfile(String username) {
        String host = config.rapidapi().host();
        String url = "https://" + host + "/v1/info?username_or_id_or_url=" + username;

        String response = restClient.get()
                .uri(url)
                .header("X-RapidAPI-Key", config.rapidapi().key())
                .header("X-RapidAPI-Host", host)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.path("data");
            if (data.isMissingNode()) {
                data = root;
            }
            return mapProfile(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse RapidAPI profile for: " + username, e);
        }
    }

    private List<InstagramProfile> parseSearchResults(String response, int limit) {
        List<InstagramProfile> profiles = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("data").path("items");
            if (items.isArray()) {
                int count = 0;
                for (JsonNode item : items) {
                    if (count >= limit) break;
                    JsonNode user = item.path("user");
                    if (!user.isMissingNode()) {
                        profiles.add(InstagramProfile.builder()
                                .username(user.path("username").asText())
                                .dataSource(DataSource.RAPIDAPI)
                                .fetchedAt(LocalDateTime.now())
                                .build());
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse RapidAPI search results", e);
        }
        return profiles;
    }

    private InstagramProfile mapProfile(JsonNode node) {
        return InstagramProfile.builder()
                .username(node.path("username").asText())
                .fullName(node.path("full_name").asText(""))
                .biography(node.path("biography").asText(""))
                .externalUrl(node.path("external_url").asText(null))
                .profilePicUrl(node.path("profile_pic_url_hd").asText(null))
                .followerCount(node.path("follower_count").asInt(0))
                .followingCount(node.path("following_count").asInt(0))
                .mediaCount(node.path("media_count").asInt(0))
                .businessAccount(node.path("is_business_account").asBoolean(false))
                .privateAccount(node.path("is_private").asBoolean(false))
                .verified(node.path("is_verified").asBoolean(false))
                .category(node.path("category_name").asText(null))
                .dataSource(DataSource.RAPIDAPI)
                .fetchedAt(LocalDateTime.now())
                .build();
    }
}
