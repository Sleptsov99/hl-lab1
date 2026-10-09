package com.example.spamer.controller;

import com.example.spamer.dto.request.SpamSendRequest;
import com.example.spamer.dto.response.SpamSendResponse;
import com.example.spamer.service.SpamSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spam")
@RequiredArgsConstructor
public class SpamController {

    private final SpamSendService service;

    @PostMapping("/send")
    public ResponseEntity<SpamSendResponse> send(@Valid @RequestBody SpamSendRequest req) {
        return ResponseEntity.ok(service.send(req));
    }
}
