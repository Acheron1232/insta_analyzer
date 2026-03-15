package com.acheron.inst_bot.service;

import com.acheron.inst_bot.model.InstagramProfile;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProfileIdentityService {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(\\+?380[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d|0[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d[\\s\\-()]*\\d)"
    );
    private static final Pattern TELEGRAM_PATTERN = Pattern.compile(
            "(?:https?://)?(?:t\\.me/|telegram\\.me/|@)([a-zA-Z0-9_]{4,32})",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern URL_WITH_SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://.+");
    private static final Set<String> NON_UNIQUE_DOMAINS = Set.of(
            "instagram.com",
            "www.instagram.com",
            "linktr.ee",
            "lnk.bio",
            "beacons.ai",
            "msha.ke",
            "taplink.cc",
            "bit.ly",
            "t.me",
            "telegram.me",
            "wa.me",
            "whatsapp.com"
    );

    public Identity buildIdentity(InstagramProfile profile) {
        String normalizedUsername = normalizeUsername(profile.getUsername());
        String websiteDomain = extractDomain(profile.getExternalUrl());
        String contactPhone = extractPhone(profile.getBiography(), profile.getExternalUrl());
        String telegramHandle = extractTelegram(profile.getExternalUrl(), profile.getBiography());
        String dedupeKey = buildDedupeKey(websiteDomain, contactPhone, telegramHandle);

        return new Identity(normalizedUsername, websiteDomain, contactPhone, telegramHandle, dedupeKey);
    }

    public String normalizeUsername(String username) {
        if (username == null) return "";
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private String buildDedupeKey(String websiteDomain, String contactPhone, String telegramHandle) {
        if (websiteDomain != null && !websiteDomain.isBlank()) {
            return "domain:" + websiteDomain;
        }
        if (contactPhone != null && !contactPhone.isBlank()) {
            return "phone:" + contactPhone;
        }
        if (telegramHandle != null && !telegramHandle.isBlank()) {
            return "telegram:" + telegramHandle;
        }
        return null;
    }

    private String extractPhone(String biography, String externalUrl) {
        String text = safe(biography) + " " + safe(externalUrl);
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        String digits = matcher.group(1).replaceAll("[^0-9+]", "");
        if (digits.startsWith("380")) {
            return "+" + digits;
        }
        if (digits.startsWith("+380")) {
            return digits;
        }
        if (digits.startsWith("0") && digits.length() == 10) {
            return "+38" + digits;
        }
        return digits;
    }

    private String extractTelegram(String externalUrl, String biography) {
        String fromUrl = findTelegramInText(safe(externalUrl));
        if (fromUrl != null) {
            return fromUrl;
        }
        return findTelegramInText(safe(biography));
    }

    private String findTelegramInText(String text) {
        Matcher matcher = TELEGRAM_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).toLowerCase(Locale.ROOT);
    }

    private String extractDomain(String externalUrl) {
        if (externalUrl == null || externalUrl.isBlank()) {
            return null;
        }

        String value = externalUrl.trim();
        if (!URL_WITH_SCHEME.matcher(value).matches()) {
            value = "https://" + value;
        }

        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return null;
            }
            host = host.toLowerCase(Locale.ROOT);
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            if (NON_UNIQUE_DOMAINS.contains(host)) {
                return null;
            }
            return host;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record Identity(
            String normalizedUsername,
            String websiteDomain,
            String contactPhone,
            String telegramHandle,
            String dedupeKey
    ) {
    }
}
