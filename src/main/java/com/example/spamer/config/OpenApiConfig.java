package com.example.spamer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI spamerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Spamer Monolith API")
                .version("v1")
                .description("Lab 1 - Spring Boot monolith. Fictional spam-dispatch billing "
                        + "domain; no messages are sent anywhere."));
    }
}
