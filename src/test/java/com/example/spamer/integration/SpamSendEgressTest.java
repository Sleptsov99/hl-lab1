package com.example.spamer.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Proves /spam/send does a real HTTP POST: we stand up a tiny loopback server,
 * point the sender's target at it, and check it actually received the requests.
 */
class SpamSendEgressTest extends AbstractIntegrationTest {

    static final AtomicInteger HITS = new AtomicInteger();
    static final HttpServer STUB;

    static {
        try {
            STUB = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            STUB.createContext("/inbox", exchange -> {
                HITS.incrementAndGet();
                byte[] reply = "{\"received\":true}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, reply.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(reply);
                }
            });
            STUB.start();
        } catch (IOException e) {
            throw new IllegalStateException("could not start stub receiver", e);
        }
    }

    @DynamicPropertySource
    static void senderTarget(DynamicPropertyRegistry registry) {
        registry.add("spamer.sender.target-url",
                () -> "http://127.0.0.1:" + STUB.getAddress().getPort() + "/inbox");
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    private String create(String path, Map<String, Object> body) throws Exception {
        return json.readTree(mvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    void sendReachesReceiverAndCharges() throws Exception {
        int before = HITS.get();
        String userId = create("/api/v1/users", Map.of(
                "username", "sender-" + System.nanoTime(),
                "email", "s" + System.nanoTime() + "@example.com",
                "role", "USER_PRO", "balance", "100.00"));
        String serviceId = create("/api/v1/services", Map.of(
                "name", "svc-" + System.nanoTime(), "pricePerMessage", "2.0000"));
        create("/api/v1/proxy", Map.of(
                "host", "10.0.0.1", "port", 8080, "protocol", "HTTP", "status", "ACTIVE"));

        mvc.perform(post("/api/v1/spam/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "userId", userId,
                                "serviceId", serviceId,
                                "victimContact", "demo@example.com",
                                "messageBody", "hello",
                                "messageCount", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SENT")))
                .andExpect(jsonPath("$.delivered", is(3)))
                .andExpect(jsonPath("$.lastHttpStatus", is(200)))
                .andExpect(jsonPath("$.remainingBalance", is(94.0)));

        // the stub actually got 3 POSTs
        org.assertj.core.api.Assertions.assertThat(HITS.get() - before)
                .isGreaterThanOrEqualTo(3);
    }

    @Test
    void builtInReceiverEchoes() throws Exception {
        mvc.perform(post("/api/v1/test-receiver/inbox")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("contact", "x", "message", "hi"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received", is(true)))
                .andExpect(jsonPath("$.echo.contact", is("x")));
    }
}
