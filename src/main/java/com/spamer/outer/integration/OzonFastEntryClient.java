package com.spamer.outer.integration;

import com.spamer.domain.ProxyEntity;
import com.spamer.outer.OuterApiResult;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OzonFastEntryClient {

    private static final String OZON_ORIGIN = "https://www.ozon.ru";
    private static final String FAST_ENTRY_URL =
            OZON_ORIGIN + "/api/composer-api.bx/_action/fastEntry";

    public OuterApiResult sendOtp(String phone, ProxyEntity proxy) {
        long started = System.currentTimeMillis();
        try {
            CookieHttpSession session = new CookieHttpSession(proxy);
            session.get(OZON_ORIGIN + "/", browserGetHeaders());

            Map<String, String> headers = browserPostHeaders();
            String body = IntegrationResponses.toJson(Map.of(
                    "phone", PhoneNormalizer.toE164(phone),
                    "otpId", 0));

            CookieHttpSession.HttpExchange response = session.postJson(FAST_ENTRY_URL, body, headers);
            if (IntegrationResponses.looksLikeOtpSent(response.body())) {
                return new OuterApiResult(response.statusCode(), response.responseTimeMs(), null);
            }
            return IntegrationResponses.fromHttp(
                    response.statusCode(), response.responseTimeMs(), response.body());
        } catch (Exception ex) {
            return new OuterApiResult(0, System.currentTimeMillis() - started, ex.getMessage());
        }
    }

    private Map<String, String> browserGetHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", BrowserHeaders.USER_AGENT);
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        return headers;
    }

    private Map<String, String> browserPostHeaders() {
        Map<String, String> headers = browserGetHeaders();
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Content-Type", "application/json");
        headers.put("Origin", OZON_ORIGIN);
        headers.put("Referer", OZON_ORIGIN + "/");
        headers.put("X-Requested-With", "XMLHttpRequest");
        return headers;
    }
}
