package com.example.spamer.dto.request;

import com.example.spamer.domain.entity.UserRole;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// All fields optional - only the ones present get applied.
public record PatchUserRequest(
        @Size(max = 64) String username,
        @Email @Size(max = 128) String email,
        UserRole role,
        @DecimalMin(value = "0.0") BigDecimal balance) {
}
