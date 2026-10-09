package com.example.spamer.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PatchServiceRequest(
        @Size(max = 128) String name,
        String description,
        @Positive BigDecimal pricePerMessage,
        Boolean active) {
}
