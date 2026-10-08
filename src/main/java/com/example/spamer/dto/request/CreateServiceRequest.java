package com.example.spamer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateServiceRequest(
        @NotBlank @Size(max = 128) String name,
        String description,
        @NotNull @Positive BigDecimal pricePerMessage,
        Boolean active) {
}
