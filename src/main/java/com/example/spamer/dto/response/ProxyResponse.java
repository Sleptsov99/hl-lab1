package com.example.spamer.dto.response;

import com.example.spamer.domain.entity.ProxyProtocol;
import com.example.spamer.domain.entity.ProxyStatus;
import java.util.UUID;

public record ProxyResponse(
        UUID id,
        String host,
        int port,
        ProxyProtocol protocol,
        String country,
        ProxyStatus status,
        UUID providerId) {
}
