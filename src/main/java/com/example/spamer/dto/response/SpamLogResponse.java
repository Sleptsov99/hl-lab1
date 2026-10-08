package com.example.spamer.dto.response;

import com.example.spamer.domain.entity.SpamStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SpamLogResponse(
        UUID id,
        UUID userId,
        String victimContact,
        String messageBody,
        SpamStatus status,
        Instant sentAt,
        Set<UUID> proxyIds) {
}
