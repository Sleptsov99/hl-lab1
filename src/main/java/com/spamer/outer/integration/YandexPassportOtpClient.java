package com.spamer.outer.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.spamer.domain.ProxyEntity;
import com.spamer.outer.OuterApiResult;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class YandexPassportOtpClient {

    private static final String PASSPORT_ORIGIN = "https://passport.yandex.ru";
    private static final String AUTH_ADD_URL = PASSPORT_ORIGIN + "/pwl-yandex/auth/add";
    private static final String BFF_BASE = PASSPORT_ORIGIN + "/pwl-yandex/api/passport/";

    private static final Pattern CSRF_PATTERN = Pattern.compile("__CSRF__\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern PROCESS_UUID_PATTERN =
            Pattern.compile("process_uuid':'([a-f0-9-]+)'");

    public OuterApiResult sendOtp(String phone, ProxyEntity proxy) {
        long started = System.currentTimeMillis();
        try {
            CookieHttpSession session = new CookieHttpSession(proxy);
            CookieHttpSession.HttpExchange page = session.get(AUTH_ADD_URL, browserHeaders(null));

            String csrf = extract(page.body(), CSRF_PATTERN, "CSRF");
            String processUuid = extract(page.body(), PROCESS_UUID_PATTERN, "process_uuid");

            Map<String, String> postHeaders = browserHeaders(csrf);
            String trackId = createTrack(session, postHeaders, processUuid);
            String nationalPhone = PhoneNormalizer.toNational(phone);

            JsonNode validated = validatePhone(session, postHeaders, trackId, nationalPhone);
            trackId = validated.path("track_id").asText(trackId);

            CookieHttpSession.HttpExchange registered = session.postJson(
                    BFF_BASE + "register/neophonish",
                    IntegrationResponses.toJson(Map.of(
                            "phone_number", nationalPhone,
                            "country", "ru",
                            "track_id", trackId,
                            "language", "ru")),
                    postHeaders);

            String registerError = IntegrationResponses.extractApiError(registered.body());
            if (registerError == null) {
                trackId = registered.body().contains("track_id")
                        ? IntegrationResponses.parseJson(registered.body()).path("track_id").asText(trackId)
                        : trackId;
            }

            CookieHttpSession.HttpExchange confirm = session.postJson(
                    BFF_BASE + "confirm_phone/submit",
                    IntegrationResponses.toJson(Map.of(
                            "track_id", trackId,
                            "confirm_method", "by_sms")),
                    postHeaders);

            if (IntegrationResponses.looksLikeOtpSent(confirm.body())) {
                return new OuterApiResult(confirm.statusCode(), confirm.responseTimeMs(), null);
            }

            OuterApiResult confirmResult = IntegrationResponses.fromHttp(
                    confirm.statusCode(), confirm.responseTimeMs(), confirm.body());
            if (confirmResult.isSuccess()) {
                return confirmResult;
            }

            if (registerError != null) {
                return new OuterApiResult(
                        registered.statusCode() >= 400 ? registered.statusCode() : 422,
                        System.currentTimeMillis() - started,
                        "register/neophonish: " + registerError);
            }

            return confirmResult;
        } catch (Exception ex) {
            return new OuterApiResult(0, System.currentTimeMillis() - started, ex.getMessage());
        }
    }

    private String createTrack(CookieHttpSession session, Map<String, String> headers, String processUuid)
            throws Exception {
        Map<String, Object> body = Map.of(
                "display_language", "ru",
                "language", "ru",
                "country", "ru",
                "retpath", PASSPORT_ORIGIN + "/profile",
                "scenario", "register",
                "track_type", "register",
                "process_uuid", processUuid,
                "device_connection_type", "wifi");

        CookieHttpSession.HttpExchange response = session.postJson(
                BFF_BASE + "track/create", IntegrationResponses.toJson(body), headers);
        String error = IntegrationResponses.extractApiError(response.body());
        if (error != null) {
            throw new IllegalStateException("track/create: " + error);
        }
        JsonNode json = IntegrationResponses.parseJson(response.body());
        String trackId = json.path("id").asText(null);
        if (trackId == null || trackId.isBlank()) {
            trackId = json.path("track_id").asText(null);
        }
        if (trackId == null || trackId.isBlank()) {
            throw new IllegalStateException("track/create: missing track id");
        }
        return trackId;
    }

    private JsonNode validatePhone(
            CookieHttpSession session,
            Map<String, String> headers,
            String trackId,
            String nationalPhone) throws Exception {
        CookieHttpSession.HttpExchange response = session.postJson(
                BFF_BASE + "validate/phone_number",
                IntegrationResponses.toJson(Map.of(
                        "phone_number", nationalPhone,
                        "country", "ru",
                        "track_id", trackId)),
                headers);
        String error = IntegrationResponses.extractApiError(response.body());
        if (error != null) {
            throw new IllegalStateException("validate/phone_number: " + error);
        }
        JsonNode json = IntegrationResponses.parseJson(response.body());
        if (!json.path("valid_for_sms").asBoolean(false)) {
            throw new IllegalStateException("Phone is not eligible for SMS on Yandex Passport");
        }
        return json;
    }

    private Map<String, String> browserHeaders(String csrf) {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", BrowserHeaders.USER_AGENT);
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.put("Origin", PASSPORT_ORIGIN);
        headers.put("Referer", AUTH_ADD_URL);
        headers.put("Content-Type", "application/json");
        if (csrf != null) {
            headers.put("X-CSRF-Token", csrf);
        }
        return headers;
    }

    private String extract(String html, Pattern pattern, String name) {
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            throw new IllegalStateException(name + " not found in Yandex Passport page");
        }
        return matcher.group(1);
    }
}
