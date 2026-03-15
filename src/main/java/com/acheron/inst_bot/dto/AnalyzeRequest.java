package com.acheron.inst_bot.dto;

import com.acheron.inst_bot.model.enums.AiProvider;
import com.acheron.inst_bot.model.enums.DataSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalyzeRequest(
        @NotBlank String username,
        @NotNull DataSource dataSource,
        @NotNull AiProvider aiProvider
) {}
