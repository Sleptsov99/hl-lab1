package com.spamer.gateway.dto;

import jakarta.validation.constraints.NotBlank;

public record BanRequest(
        @NotBlank String target,
        @NotBlank String reason) {
}
