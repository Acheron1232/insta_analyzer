package com.acheron.inst_bot.service.instagram;

import com.acheron.inst_bot.config.InstagramSourceConfig;
import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.model.enums.DataSource;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.models.user.User;
import com.github.instagram4j.instagram4j.requests.feed.FeedTagRequest;
import com.github.instagram4j.instagram4j.requests.users.UsersUsernameInfoRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedTagResponse;
import com.github.instagram4j.instagram4j.responses.users.UserResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class Instagram4jDataSource implements InstagramDataSource {

    private final InstagramSourceConfig config;
    private IGClient client;

    @PostConstruct
    void init() {
        String username = config.instagram4j().username();
        String password = config.instagram4j().password();
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            try {
                client = IGClient.builder()
                        .username(username)
                        .password(password)
                        .login();
                log.info("Instagram4j client logged in as: {}", username);
            } catch (Exception e) {
                log.warn("Instagram4j login failed — this data source will be unavailable: {}", e.getMessage());
            }
        } else {
            log.info("Instagram4j credentials not configured — skipping login");
        }
    }

    @Override
    public DataSource getType() {
        return DataSource.INSTAGRAM4J;
    }

    @Override
    public List<InstagramProfile> searchProfiles(String query, int limit) {
        ensureLoggedIn();
        List<InstagramProfile> profiles = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try {
            FeedTagResponse response = client.sendRequest(new FeedTagRequest(query)).join();
            if (response.getItems() != null) {
                for (var item : response.getItems()) {
                    if (profiles.size() >= limit) break;
                    Profile user = item.getUser();
                    if (user != null && seen.add(user.getUsername())) {
                        profiles.add(InstagramProfile.builder()
                                .username(user.getUsername())
                                .fullName(user.getFull_name())
                                .profilePicUrl(user.getProfile_pic_url())
                                .privateAccount(user.is_private())
                                .dataSource(DataSource.INSTAGRAM4J)
                                .fetchedAt(LocalDateTime.now())
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Instagram4j hashtag search failed for: {}", query, e);
        }
        return profiles;
    }

    @Override
    public InstagramProfile fetchProfile(String username) {
        ensureLoggedIn();
        try {
            UserResponse response = client.sendRequest(new UsersUsernameInfoRequest(username)).join();
            User user = response.getUser();
            return InstagramProfile.builder()
                    .username(user.getUsername())
                    .fullName(user.getFull_name())
                    .biography(user.getBiography())
                    .externalUrl(user.getExternal_url())
                    .profilePicUrl(user.getProfile_pic_url())
                    .followerCount(user.getFollower_count())
                    .followingCount(user.getFollowing_count())
                    .mediaCount(user.getMedia_count())
                    .businessAccount(user.is_business())
                    .privateAccount(user.is_private())
                    .verified(user.is_verified())
                    .category(null)
                    .dataSource(DataSource.INSTAGRAM4J)
                    .fetchedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Instagram4j failed to fetch profile: " + username, e);
        }
    }

    private void ensureLoggedIn() {
        if (client == null) {
            throw new IllegalStateException("Instagram4j client is not logged in. Configure IG_USERNAME and IG_PASSWORD.");
        }
    }
}
