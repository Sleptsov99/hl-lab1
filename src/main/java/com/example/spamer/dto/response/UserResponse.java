package com.example.spamer.dto.response;

import com.example.spamer.domain.entity.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        UserRole role,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt) {
}
