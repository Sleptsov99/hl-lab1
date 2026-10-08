package com.example.spamer.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class ApiFlowTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    private String createUser(String username, String email, String role, String balance)
            throws Exception {
        MvcResult res = mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "username", username,
                                "email", email,
                                "role", role,
                                "balance", balance))))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(res.getResponse().getContentAsString()).get("id").asText();
    }

    private String createService(String name, String price) throws Exception {
        MvcResult res = mvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "name", name,
                                "description", "desc",
                                "pricePerMessage", price))))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(res.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void userCrudLifecycle() throws Exception {
        String id = createUser("alice", "alice@example.com", "USER_FREE", "10.00");

        mvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("alice")));

        mvc.perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "username", "alice2",
                                "email", "alice2@example.com",
                                "role", "USER_PRO",
                                "balance", "5.00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("USER_PRO")));

        mvc.perform(patch("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("balance", "99.50"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(99.50)))
                .andExpect(jsonPath("$.username", is("alice2")));

        mvc.perform(get("/api/v1/users").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));

        mvc.perform(delete("/api/v1/users/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/users/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void duplicateUsernameConflicts() throws Exception {
        createUser("bob", "bob@example.com", "USER_FREE", "0");
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "username", "bob",
                                "email", "other@example.com",
                                "role", "USER_FREE"))))
                .andExpect(status().isConflict());
    }

    @Test
    void validationErrorHasDetails() throws Exception {
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "username", "",
                                "email", "not-an-email",
                                "role", "USER_FREE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", hasSize(org.hamcrest.Matchers.greaterThan(0))));
    }

    @Test
    void sizeAboveLimitRejected() throws Exception {
        mvc.perform(get("/api/v1/users").param("size", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void spamSendRollsBackWhenBalanceTooLow() throws Exception {
        String userId = createUser("dave", "dave@example.com", "USER_PRO", "1.00");
        String serviceId = createService("pricey-" + System.nanoTime(), "5.0000");

        mvc.perform(post("/api/v1/spam/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "userId", userId,
                                "serviceId", serviceId,
                                "victimContact", "x@example.com",
                                "messageCount", 1))))
                .andExpect(status().isUnprocessableEntity());

        // balance untouched after rollback
        mvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(jsonPath("$.balance", is(1.0)));
    }

    @Test
    void subscribePromotesFreeUserToPro() throws Exception {
        String userId = createUser("erin", "erin@example.com", "USER_FREE", "0");
        String serviceId = createService("sub-" + System.nanoTime(), "1.0000");

        mvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "userId", userId,
                                "serviceId", serviceId,
                                "discountPercent", "10.00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discountPercent", is(10.0)));

        mvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(jsonPath("$.role", is("USER_PRO")));

        // duplicate subscription rejected
        mvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "userId", userId,
                                "serviceId", serviceId))))
                .andExpect(status().isConflict());
    }

    @Test
    void spamLogCursorFeedPaginates() throws Exception {
        String userId = createUser("frank", "frank@example.com", "USER_PRO", "0");
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/v1/spam-log")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(Map.of(
                                    "userId", userId,
                                    "victimContact", "c" + i + "@example.com",
                                    "status", "QUEUED"))))
                    .andExpect(status().isCreated());
        }

        MvcResult first = mvc.perform(get("/api/v1/spam-log/feed").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andReturn();

        JsonNode node = json.readTree(first.getResponse().getContentAsString());
        String cursor = node.get("nextCursor").asText();

        mvc.perform(get("/api/v1/spam-log/feed").param("cursor", cursor).param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").exists());
    }

    @Test
    void unknownUserGives404() throws Exception {
        mvc.perform(get("/api/v1/users/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
