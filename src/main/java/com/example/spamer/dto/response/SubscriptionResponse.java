package com.example.spamer.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
        UUID userId,
        UUID serviceId,
        Instant subscribedAt,
        BigDecimal discountPercent) {
}
