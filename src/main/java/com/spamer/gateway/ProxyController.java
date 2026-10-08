package com.spamer.gateway;

import com.spamer.gateway.dto.CreateProxyRequest;
import com.spamer.proxy.ProxyProviderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proxy")
@PreAuthorize("hasRole('ADMIN')")
public class ProxyController {

    private final ProxyProviderService proxyProviderService;

    public ProxyController(ProxyProviderService proxyProviderService) {
        this.proxyProviderService = proxyProviderService;
    }

    @GetMapping
    public ResponseEntity<List<?>> list() {
        return ResponseEntity.ok(proxyProviderService.listEnabled());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateProxyRequest request) {
        return ResponseEntity.ok(proxyProviderService.create(
                request.host(),
                request.port(),
                request.username(),
                request.password()));
    }
}
