package com.spamer.gateway.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProxyRequest(
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        String username,
        String password) {
}
