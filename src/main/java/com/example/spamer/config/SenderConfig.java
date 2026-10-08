package com.example.spamer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class SenderConfig {

    @Value("${spamer.sender.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${spamer.sender.read-timeout-ms:5000}")
    private int readTimeoutMs;

    // Dedicated client with tight timeouts - the receiver is a test box, we don't
    // want a hung demo blocking a request for a minute.
    @Bean
    public RestClient senderRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(connectTimeoutMs);
        rf.setReadTimeout(readTimeoutMs);
        return builder.requestFactory(rf).build();
    }
}
