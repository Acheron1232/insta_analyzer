package com.acheron.inst_bot.dto;

import com.acheron.inst_bot.model.ScanJob;
import lombok.Builder;

@Builder
public record ScanJobDto(
        Long id,
        String searchQuery,
        String dataSource,
        String aiProvider,
        String status,
        int profilesFound,
        int profilesAnalyzed,
        int leadsFound,
        String startedAt,
        String completedAt,
        String errorMessage
) {
    public static ScanJobDto from(ScanJob j) {
        return ScanJobDto.builder()
                .id(j.getId())
                .searchQuery(j.getSearchQuery())
                .dataSource(j.getDataSource() != null ? j.getDataSource().name() : null)
                .aiProvider(j.getAiProvider() != null ? j.getAiProvider().name() : null)
                .status(j.getStatus() != null ? j.getStatus().name() : null)
                .profilesFound(j.getProfilesFound())
                .profilesAnalyzed(j.getProfilesAnalyzed())
                .leadsFound(j.getLeadsFound())
                .startedAt(j.getStartedAt() != null ? j.getStartedAt().toString() : null)
                .completedAt(j.getCompletedAt() != null ? j.getCompletedAt().toString() : null)
                .errorMessage(j.getErrorMessage())
                .build();
    }
}
