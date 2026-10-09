package com.example.spamer.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        String description,
        BigDecimal pricePerMessage,
        boolean active) {
}
