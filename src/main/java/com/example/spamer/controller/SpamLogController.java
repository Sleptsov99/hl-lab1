package com.example.spamer.controller;

import com.example.spamer.dto.request.CreateSpamLogRequest;
import com.example.spamer.dto.request.PatchSpamLogRequest;
import com.example.spamer.dto.response.CursorPage;
import com.example.spamer.dto.response.SpamLogResponse;
import com.example.spamer.service.SpamLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spam-log")
@Validated
@RequiredArgsConstructor
public class SpamLogController {

    private final SpamLogService service;

    @PostMapping
    public ResponseEntity<SpamLogResponse> create(@Valid @RequestBody CreateSpamLogRequest req) {
        SpamLogResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/spam-log/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpamLogResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<SpamLogResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(service.count()))
                .body(service.list(page, size));
    }

    // Cursor feed for infinite scroll - no total count exposed by design.
    @GetMapping("/feed")
    public ResponseEntity<CursorPage<SpamLogResponse>> feed(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok(service.feed(cursor, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpamLogResponse> replace(
            @PathVariable UUID id, @Valid @RequestBody CreateSpamLogRequest req) {
        return ResponseEntity.ok(service.replace(id, req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SpamLogResponse> patch(
            @PathVariable UUID id, @Valid @RequestBody PatchSpamLogRequest req) {
        return ResponseEntity.ok(service.patch(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
