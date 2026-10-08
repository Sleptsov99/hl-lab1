package com.spamer.auth;

import com.spamer.domain.UserEntity;
import com.spamer.domain.UserRole;
import com.spamer.domain.UserTier;
import com.spamer.repository.UserRepository;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(String username, String password) {
        UserEntity user = userRepository.findByUsername(username)
                .filter(UserEntity::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getTier().name());

        return Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "tier", user.getTier().name());
    }

    @Transactional
    public UserEntity register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        user.setTier(UserTier.FREE);
        return userRepository.save(user);
    }
}
