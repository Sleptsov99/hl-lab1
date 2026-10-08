package com.spamer.outer.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spamer.outer.OuterApiResult;

public final class IntegrationResponses {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private IntegrationResponses() {
    }

    public static OuterApiResult fromHttp(int statusCode, long responseTimeMs, String body) {
        if (statusCode < 200 || statusCode >= 300) {
            return new OuterApiResult(statusCode, responseTimeMs, truncate(body));
        }
        String apiError = extractApiError(body);
        if (apiError != null) {
            return new OuterApiResult(422, responseTimeMs, apiError);
        }
        return new OuterApiResult(statusCode, responseTimeMs, null);
    }

    public static String extractApiError(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(body);
            if (node.has("error") && !node.get("error").isNull()) {
                return node.get("error").asText();
            }
            if (node.has("errors") && node.get("errors").isArray() && !node.get("errors").isEmpty()) {
                return node.get("errors").get(0).asText();
            }
            if (node.has("status") && "error".equalsIgnoreCase(node.get("status").asText())) {
                if (node.has("code")) {
                    return node.get("code").asText();
                }
                return "api status=error";
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    public static boolean looksLikeOtpSent(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        try {
            JsonNode node = MAPPER.readTree(body);
            if (node.has("deny_resend_until")) {
                return true;
            }
            if (node.has("code") && node.get("code").has("length")) {
                return true;
            }
            if (node.has("code_length")) {
                return true;
            }
            if (node.has("otpSent") && node.get("otpSent").asBoolean(false)) {
                return true;
            }
            if (node.has("result") && node.get("result").asText("").toLowerCase().contains("sent")) {
                return true;
            }
            if (node.has("timeToLive") && node.has("retryDelay")) {
                return true;
            }
            if (node.has("data") && node.get("data").has("code")
                    && "fieldsIsCorrect".equals(node.get("data").get("code").asText())) {
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public static OuterApiResult fromOtpExchange(CookieHttpSession.HttpExchange exchange) {
        if (exchange.statusCode() == 204 || IntegrationResponses.looksLikeOtpSent(exchange.body())) {
            return new OuterApiResult(exchange.statusCode(), exchange.responseTimeMs(), null);
        }
        return IntegrationResponses.fromHttp(exchange.statusCode(), exchange.responseTimeMs(), exchange.body());
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize JSON", ex);
        }
    }

    public static JsonNode parseJson(String body) {
        try {
            return MAPPER.readTree(body);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse JSON: " + truncate(body), ex);
        }
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 512 ? value.substring(0, 512) : value;
    }
}
