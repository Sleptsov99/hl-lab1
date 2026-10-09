package com.example.spamer.dto.response;

import com.example.spamer.domain.entity.SpamStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record SpamSendResponse(
        UUID spamLogId,
        SpamStatus status,
        BigDecimal charged,
        BigDecimal remainingBalance,
        int delivered,
        String target,
        int lastHttpStatus) {
}
