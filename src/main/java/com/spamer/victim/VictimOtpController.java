package com.spamer.victim;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/victim")
public class VictimOtpController {

    private static final Logger log = LoggerFactory.getLogger(VictimOtpController.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @PostMapping("/{provider}/otp")
    public Map<String, Object> sendOtp(
            @PathVariable String provider,
            @RequestBody(required = false) Map<String, Object> body) {
        String phone = extractPhone(body);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        log.info("[{}] OTP {} -> phone {}", provider, code, phone);
        return Map.of(
                "provider", provider,
                "phone", phone,
                "otpSent", true,
                "code", code,
                "timestamp", Instant.now().toString());
    }

    @PostMapping("/{provider}/password-reset")
    public Map<String, Object> resetPassword(
            @PathVariable String provider,
            @RequestBody(required = false) Map<String, Object> body) {
        String phone = extractPhone(body);
        log.info("[{}] password reset requested for phone {}", provider, phone);
        return Map.of(
                "provider", provider,
                "phone", phone,
                "resetInitiated", true,
                "timestamp", Instant.now().toString());
    }

    private String extractPhone(Map<String, Object> body) {
        if (body == null) {
            return "";
        }
        Object phone = body.get("phone");
        if (phone == null) {
            phone = body.get("email");
        }
        return phone != null ? phone.toString() : "";
    }
}
