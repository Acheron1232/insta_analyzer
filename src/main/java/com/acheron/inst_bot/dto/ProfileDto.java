package com.acheron.inst_bot.dto;

import com.acheron.inst_bot.model.InstagramProfile;
import lombok.Builder;

@Builder
public record ProfileDto(
        Long id,
        String username,
        String fullName,
        String biography,
        String externalUrl,
        String websiteDomain,
        String contactPhone,
        String telegramHandle,
        String profilePicUrl,
        int followerCount,
        int mediaCount,
        boolean businessAccount,
        String category,
        String dataSource,
        String fetchedAt,
        AnalysisResultDto analysis
) {
    public static ProfileDto from(InstagramProfile p) {
        return ProfileDto.builder()
                .id(p.getId())
                .username(p.getUsername())
                .fullName(p.getFullName())
                .biography(p.getBiography())
                .externalUrl(p.getExternalUrl())
                .websiteDomain(p.getWebsiteDomain())
                .contactPhone(p.getContactPhone())
                .telegramHandle(p.getTelegramHandle())
                .profilePicUrl(p.getProfilePicUrl())
                .followerCount(p.getFollowerCount())
                .mediaCount(p.getMediaCount())
                .businessAccount(p.isBusinessAccount())
                .category(p.getCategory())
                .dataSource(p.getDataSource() != null ? p.getDataSource().name() : null)
                .fetchedAt(p.getFetchedAt() != null ? p.getFetchedAt().toString() : null)
                .analysis(p.getAnalysisResult() != null ? AnalysisResultDto.from(p.getAnalysisResult()) : null)
                .build();
    }
}
