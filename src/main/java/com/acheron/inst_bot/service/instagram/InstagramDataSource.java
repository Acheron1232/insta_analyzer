package com.acheron.inst_bot.service.instagram;

import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.model.enums.DataSource;

import java.util.List;

public interface InstagramDataSource {

    DataSource getType();

    List<InstagramProfile> searchProfiles(String query, int limit);

    InstagramProfile fetchProfile(String username);
}
