package com.example.spamer.dto.request;

import com.example.spamer.domain.entity.SpamStatus;
import jakarta.validation.constraints.Size;

public record PatchSpamLogRequest(
        @Size(max = 256) String victimContact,
        String messageBody,
        SpamStatus status) {
}
