package com.acheron.inst_bot.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class UkraineQueryPlanner {

    private static final List<String> DEFAULT_NICHES = List.of(
            "beauty", "nails", "barbershop", "coffee", "bakery",
            "flowers", "clothes", "kids", "handmade", "furniture"
    );

    private static final List<String> DEFAULT_CITIES = List.of(
            "kyiv", "lviv", "odesa", "dnipro", "kharkiv", "vinnytsia"
    );

    public List<String> buildQueries(List<String> rawNiches, List<String> rawCities) {
        List<String> niches = normalizeList(rawNiches, DEFAULT_NICHES);
        List<String> cities = normalizeList(rawCities, DEFAULT_CITIES);

        Set<String> queries = new LinkedHashSet<>();
        queries.add("ukraine_small_business");
        queries.add("made_in_ukraine");
        queries.add("ukrainian_brand");

        for (String city : cities) {
            queries.add(city);
            queries.add(city + "_business");
            queries.add(city + "_shop");
        }

        for (String niche : niches) {
            queries.add(niche + "_ua");
            queries.add(niche + "_ukraine");
        }

        for (String city : cities) {
            for (String niche : niches) {
                queries.add(niche + "_" + city);
            }
        }

        return queries.stream().limit(40).toList();
    }

    private List<String> normalizeList(List<String> values, List<String> defaults) {
        if (values == null || values.isEmpty()) {
            return defaults;
        }

        return values.stream()
                .map(this::sanitize)
                .filter(v -> !v.isBlank())
                .limit(20)
                .toList();
    }

    private String sanitize(String value) {
        if (value == null) return "";
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replace("@", "")
                .replace("#", "")
                .replace(' ', '_')
                .replace('-', '_');
        return normalized.replaceAll("[^a-z0-9_]", "");
    }
}
