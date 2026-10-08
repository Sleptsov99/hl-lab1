package com.spamer.gateway;

import com.spamer.admin.AdminService;
import com.spamer.domain.UserEntity;
import com.spamer.domain.UserRole;
import com.spamer.domain.UserTier;
import com.spamer.gateway.dto.CreateServiceRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> users() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PostMapping("/users/{id}/tier")
    public ResponseEntity<UserEntity> setTier(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        UserTier tier = UserTier.valueOf(body.get("tier"));
        return ResponseEntity.ok(adminService.updateUserTier(id, tier));
    }

    @PostMapping("/users/{id}/role")
    public ResponseEntity<UserEntity> setRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        UserRole role = UserRole.valueOf(body.get("role"));
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    @GetMapping("/services")
    public ResponseEntity<List<?>> services() {
        return ResponseEntity.ok(adminService.listServices());
    }

    @PostMapping("/services")
    public ResponseEntity<?> createService(@Valid @RequestBody CreateServiceRequest request) {
        return ResponseEntity.ok(adminService.createService(
                request.name(),
                request.baseUrl(),
                request.endpointType(),
                request.endpointPath(),
                request.httpMethod(),
                request.requestBodyTemplate()));
    }
}
