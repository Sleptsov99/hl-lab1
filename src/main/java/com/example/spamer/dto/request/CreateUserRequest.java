package com.example.spamer.dto.request;

import com.example.spamer.domain.entity.UserRole;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateUserRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Email @Size(max = 128) String email,
        @NotNull UserRole role,
        @DecimalMin(value = "0.0") BigDecimal balance) {
}
