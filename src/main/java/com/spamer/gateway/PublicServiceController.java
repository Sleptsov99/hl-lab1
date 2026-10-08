package com.spamer.gateway;

import com.spamer.repository.ServiceRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class PublicServiceController {

    private final ServiceRepository serviceRepository;

    public PublicServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public ResponseEntity<List<?>> listEnabled() {
        return ResponseEntity.ok(serviceRepository.findByEnabledTrue());
    }
}
