package com.example.spamer.controller;

import com.example.spamer.dto.request.SubscriptionRequest;
import com.example.spamer.dto.response.SubscriptionResponse;
import com.example.spamer.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscriptionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.subscribe(req));
    }
}
