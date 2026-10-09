package com.example.spamer.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Does the actual HTTP POST for a single message. The destination comes only
 * from config ({@code spamer.sender.target-url}) - never from the request - so
 * this can only ever talk to the one receiver the operator points it at
 * (the built-in test endpoint, or your own test server via SENDER_TARGET_URL).
 */
@Service
@RequiredArgsConstructor
public class MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private final RestClient client;
    private final Environment env;

    public SendResult send(String contact, String body, int index) {
        // read lazily so the target can be re-pointed via config without a rebuild
        String target = env.getProperty("spamer.sender.target-url");
        long started = System.currentTimeMillis();
        try {
            ResponseEntity<String> resp = client.post()
                    .uri(target)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "contact", contact,
                            "message", body == null ? "" : body,
                            "index", index))
                    .retrieve()
                    .toEntity(String.class);
            long ms = System.currentTimeMillis() - started;
            log.info("sent #{} to {} -> HTTP {} ({}ms)",
                    index, target, resp.getStatusCode().value(), ms);
            return new SendResult(
                    resp.getStatusCode().value(),
                    resp.getStatusCode().is2xxSuccessful(),
                    snippet(resp.getBody()),
                    ms);
        } catch (RestClientResponseException e) {
            // receiver answered, but with an error status
            long ms = System.currentTimeMillis() - started;
            log.warn("sent #{} to {} -> HTTP {}", index, target, e.getStatusCode().value());
            return new SendResult(
                    e.getStatusCode().value(), false, snippet(e.getResponseBodyAsString()), ms);
        } catch (Exception e) {
            // couldn't reach it at all (down, DNS, timeout)
            long ms = System.currentTimeMillis() - started;
            log.warn("send #{} to {} failed: {}", index, target, e.getMessage());
            return new SendResult(0, false, e.getMessage(), ms);
        }
    }

    private static String snippet(String s) {
        if (s == null) {
            return null;
        }
        String flat = s.replaceAll("\\s+", " ").trim();
        return flat.length() > 200 ? flat.substring(0, 200) : flat;
    }

    public record SendResult(int httpStatus, boolean ok, String responseSnippet, long latencyMs) {
    }
}
