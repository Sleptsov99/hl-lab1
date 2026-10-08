package com.example.spamer.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionRequest(
        @NotNull UUID userId,
        @NotNull UUID serviceId,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discountPercent) {
}
