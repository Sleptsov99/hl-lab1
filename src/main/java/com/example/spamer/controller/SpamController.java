package com.example.spamer.controller;

import com.example.spamer.dto.request.SpamSendRequest;
import com.example.spamer.dto.response.SpamSendResponse;
import com.example.spamer.service.SpamSendService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spam")
public class SpamController {

    private final SpamSendService service;

    public SpamController(SpamSendService service) {
        this.service = service;
    }

    @PostMapping("/send")
    public SpamSendResponse send(@Valid @RequestBody SpamSendRequest req) {
        return service.send(req);
    }
}
