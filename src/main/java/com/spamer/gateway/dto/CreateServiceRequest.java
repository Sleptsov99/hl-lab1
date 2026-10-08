package com.spamer.gateway.dto;

import com.spamer.domain.EndpointType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateServiceRequest(
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotNull EndpointType endpointType,
        @NotBlank String endpointPath,
        @NotBlank String httpMethod,
        String requestBodyTemplate) {
}
