package com.example.spamer.controller;

import com.example.spamer.dto.request.CreateServiceRequest;
import com.example.spamer.dto.request.PatchServiceRequest;
import com.example.spamer.dto.response.ServiceResponse;
import com.example.spamer.service.ServiceCatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/v1/services")
@Validated
public class ServiceController {

    private final ServiceCatalogService service;

    public ServiceController(ServiceCatalogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest req) {
        ServiceResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/services/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ServiceResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(service.count()))
                .body(service.list(page, size));
    }

    @PutMapping("/{id}")
    public ServiceResponse replace(
            @PathVariable UUID id, @Valid @RequestBody CreateServiceRequest req) {
        return service.replace(id, req);
    }

    @PatchMapping("/{id}")
    public ServiceResponse patch(
            @PathVariable UUID id, @Valid @RequestBody PatchServiceRequest req) {
        return service.patch(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
