package com.spamer.gateway;

import com.spamer.ban.BanService;
import com.spamer.gateway.dto.BanRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ban")
@PreAuthorize("hasAnyRole('BAN_OPERATOR', 'ADMIN')")
public class BanController {

    private final BanService banService;

    public BanController(BanService banService) {
        this.banService = banService;
    }

    @GetMapping
    public ResponseEntity<List<?>> list() {
        return ResponseEntity.ok(banService.listAll());
    }

    @PostMapping
    public ResponseEntity<?> ban(Principal principal, @Valid @RequestBody BanRequest request) {
        return ResponseEntity.ok(banService.ban(
                request.target(), request.reason(), principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unban(@PathVariable Long id) {
        banService.unban(id);
        return ResponseEntity.noContent().build();
    }
}
