package com.acheron.inst_bot.dto;

import com.acheron.inst_bot.model.enums.AiProvider;
import com.acheron.inst_bot.model.enums.DataSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UkraineScanRequest(
        @NotNull DataSource dataSource,
        @NotNull AiProvider aiProvider,
        @Min(1) @Max(500) int limit,
        List<@NotBlank String> niches,
        List<@NotBlank String> cities
) {
}
