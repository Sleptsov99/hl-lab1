package com.spamer.gateway;

import com.spamer.domain.SpamCampaignEntity;
import com.spamer.gateway.dto.CreateCampaignRequest;
import com.spamer.spam.SpamService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spam")
public class SpamController {

    private final SpamService spamService;

    public SpamController(SpamService spamService) {
        this.spamService = spamService;
    }

    @PostMapping("/campaigns")
    public ResponseEntity<SpamCampaignEntity> create(
            Principal principal,
            @Valid @RequestBody CreateCampaignRequest request) {
        SpamCampaignEntity campaign = spamService.createCampaign(
                principal.getName(),
                request.name(),
                request.serviceId(),
                request.targetEmail(),
                request.totalRequests(),
                request.ratePerSecond(),
                request.concurrency());
        return ResponseEntity.ok(campaign);
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<SpamCampaignEntity>> list(Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(spamService.listCampaigns(auth.getName(), admin));
    }

    @PostMapping("/campaigns/{id}/stop")
    public ResponseEntity<SpamCampaignEntity> stop(@PathVariable Long id, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(spamService.stopCampaign(id, auth.getName(), admin));
    }

    @GetMapping("/campaigns/{id}/logs")
    public ResponseEntity<List<?>> logs(@PathVariable Long id) {
        return ResponseEntity.ok(spamService.getCampaignLogs(id));
    }

    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> limits(Principal principal) {
        var tier = spamService.getUserTier(principal.getName());
        return ResponseEntity.ok(Map.of("tier", tier.name()));
    }
}
