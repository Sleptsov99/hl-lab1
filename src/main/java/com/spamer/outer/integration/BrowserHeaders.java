package com.spamer.outer.integration;

import java.net.http.HttpRequest;
import java.util.Map;

public final class BrowserHeaders {

    public static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36";

    private BrowserHeaders() {
    }

    public static void applyDefaults(HttpRequest.Builder builder, String origin, String referer) {
        builder.header("User-Agent", USER_AGENT);
        builder.header("Accept", "application/json, text/plain, */*");
        builder.header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        if (origin != null) {
            builder.header("Origin", origin);
        }
        if (referer != null) {
            builder.header("Referer", referer);
        }
    }

    public static void applyJsonPost(HttpRequest.Builder builder, String origin, String referer) {
        applyDefaults(builder, origin, referer);
        builder.header("Content-Type", "application/json");
    }

    public static void applyJsonPost(
            HttpRequest.Builder builder,
            String origin,
            String referer,
            Map<String, String> extra) {
        applyJsonPost(builder, origin, referer);
        extra.forEach(builder::header);
    }
}
