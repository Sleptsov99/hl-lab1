package com.example.spamer.dto.request;

import com.example.spamer.domain.entity.ProxyProtocol;
import com.example.spamer.domain.entity.ProxyStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateProxyRequest(
        @NotBlank @Size(max = 128) String host,
        @Min(1) @Max(65535) int port,
        @NotNull ProxyProtocol protocol,
        @Size(max = 64) String country,
        @NotNull ProxyStatus status,
        UUID providerId) {
}
