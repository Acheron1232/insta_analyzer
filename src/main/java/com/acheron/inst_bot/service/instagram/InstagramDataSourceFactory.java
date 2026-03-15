package com.acheron.inst_bot.service.instagram;

import com.acheron.inst_bot.model.enums.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InstagramDataSourceFactory {

    private final List<InstagramDataSource> sources;

    public InstagramDataSource get(DataSource type) {
        return sources.stream()
                .filter(s -> s.getType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown data source: " + type));
    }
}
