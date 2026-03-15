package com.acheron.inst_bot.service.ai;

import com.acheron.inst_bot.model.InstagramProfile;

import java.util.List;
import java.util.stream.Collectors;

public final class AnalysisPrompt {

    private AnalysisPrompt() {}

    /**
     * Build a prompt for batch analysis of multiple profiles in one AI call.
     */
    public static String buildBatch(List<InstagramProfile> profiles) {
        String profilesJson = profiles.stream()
                .map(AnalysisPrompt::profileToJson)
                .collect(Collectors.joining(",\n    "));

        return """
                You are an Instagram business analyst. Analyze the following batch of Instagram profiles.
                Determine which ones are Ukraine-based small businesses that could benefit from a website, CRM, or Telegram bot.

                PROFILES:
                [
                    %s
                ]

                For EACH profile, evaluate and return a JSON array with results.
                Respond with ONLY a valid JSON array (no markdown, no explanation outside JSON):
                [
                  {
                    "username": "the_username",
                    "isSmallBusiness": true/false,
                    "ukraineBased": true/false,
                    "lacksWebsite": true/false,
                    "lacksCrm": true/false,
                    "lacksTelegramBot": true/false,
                    "sellsViaDms": true/false,
                    "isActive": true/false,
                    "detectedCategory": "business type",
                    "overallScore": 0-100,
                    "reasoning": "brief 1-2 sentence explanation"
                  }
                ]

                Scoring guidelines:
                - High score (70-100): Ukraine small business, 3K-50K followers, no website, no CRM, no Telegram bot, likely sells via DMs, active
                - Medium score (40-69): business indicators exist but one major gap (not Ukraine, has website/CRM/TG bot, or weak activity)
                - Low score (0-39): personal account, influencer, large brand, or inactive

                Key DM-selling indicators: bio mentions "DM", "замовлення", "заказ", "напишіть", "order", "WhatsApp", "Viber", "пишіть в дірект", "ціна в дірект"
                Lacking website indicators: no external URL, or URL is just linktree/linktr.ee
                Lacking CRM indicators: no CRM wording or links (bitrix24, amocrm/kommo, hubspot, zoho, pipedrive, salesforce, "crm")
                Lacking TG bot indicators: no t.me link in bio, no mention of Telegram bot
                Ukraine-based indicators: bio/city mentions (Kyiv/Lviv/Odesa/Dnipro/etc), Ukrainian language, +380 phone, .ua links, or explicit Ukraine mention

                Return EXACTLY %d results in the array, one per profile, in the same order.
                """.formatted(profilesJson, profiles.size());
    }

    /**
     * Build a prompt for a single profile (wraps it as batch of 1).
     */
    public static String buildSingle(InstagramProfile profile) {
        return buildBatch(List.of(profile));
    }

    private static String profileToJson(InstagramProfile p) {
        return """
                {"username": "%s", "fullName": "%s", "bio": "%s", "followers": %d, "following": %d, "posts": %d, "externalUrl": "%s", "isBusinessAccount": %s, "category": "%s", "lastPostDate": "%s"}""".formatted(
                escape(p.getUsername()),
                escape(p.getFullName()),
                escape(p.getBiography()),
                p.getFollowerCount(),
                p.getFollowingCount(),
                p.getMediaCount(),
                p.getExternalUrl() != null ? escape(p.getExternalUrl()) : "none",
                p.isBusinessAccount(),
                p.getCategory() != null ? escape(p.getCategory()) : "unknown",
                p.getLastPostAt() != null ? p.getLastPostAt().toString() : "unknown"
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", "");
    }
}
