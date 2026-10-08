package com.spamer.gateway.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCampaignRequest(
        @NotBlank String name,
        @NotNull Long serviceId,
        @NotBlank String targetEmail,
        @Min(1) @Max(100000) int totalRequests,
        @Min(1) int ratePerSecond,
        @Min(1) @Max(50) int concurrency) {
}
