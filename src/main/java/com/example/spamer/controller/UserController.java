package com.example.spamer.controller;

import com.example.spamer.dto.request.CreateUserRequest;
import com.example.spamer.dto.request.PatchUserRequest;
import com.example.spamer.dto.response.UserResponse;
import com.example.spamer.service.UserService;
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
@RequestMapping("/api/v1/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        UserResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        List<UserResponse> body = service.list(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(service.count()))
                .body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> replace(
            @PathVariable UUID id, @Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(service.replace(id, req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patch(
            @PathVariable UUID id, @Valid @RequestBody PatchUserRequest req) {
        return ResponseEntity.ok(service.patch(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
