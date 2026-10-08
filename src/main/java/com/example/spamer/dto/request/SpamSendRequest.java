package com.example.spamer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SpamSendRequest(
        @NotNull UUID userId,
        @NotNull UUID serviceId,
        @NotBlank @Size(max = 256) String victimContact,
        String messageBody,
        @Min(1) @Max(100) int messageCount) {
}
