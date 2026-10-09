package com.example.spamer.dto.request;

import com.example.spamer.domain.entity.SpamStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateSpamLogRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 256) String victimContact,
        String messageBody,
        @NotNull SpamStatus status) {
}
