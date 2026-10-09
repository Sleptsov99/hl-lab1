package com.example.spamer.service;

import com.example.spamer.domain.entity.UserEntity;
import com.example.spamer.domain.repository.UserRepository;
import com.example.spamer.dto.request.CreateUserRequest;
import com.example.spamer.dto.request.PatchUserRequest;
import com.example.spamer.dto.response.UserResponse;
import com.example.spamer.exception.ConflictException;
import com.example.spamer.exception.NotFoundException;
import com.example.spamer.mapper.UserMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final UserMapper mapper;

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        if (repo.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken: " + req.username());
        }
        if (repo.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered: " + req.email());
        }
        UserEntity u = new UserEntity();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setRole(req.role());
        u.setBalance(req.balance() != null ? req.balance() : BigDecimal.ZERO);
        return mapper.toResponse(repo.save(u));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(int page, int size) {
        return repo.findAll(PageRequest.of(page, size)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return repo.count();
    }

    @Transactional
    public UserResponse replace(UUID id, CreateUserRequest req) {
        UserEntity u = find(id);
        if (!u.getUsername().equals(req.username()) && repo.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken: " + req.username());
        }
        if (!u.getEmail().equals(req.email()) && repo.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered: " + req.email());
        }
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setRole(req.role());
        u.setBalance(req.balance() != null ? req.balance() : BigDecimal.ZERO);
        return mapper.toResponse(u);
    }

    @Transactional
    public UserResponse patch(UUID id, PatchUserRequest req) {
        UserEntity u = find(id);
        if (req.username() != null && !req.username().equals(u.getUsername())) {
            if (repo.existsByUsername(req.username())) {
                throw new ConflictException("Username already taken: " + req.username());
            }
            u.setUsername(req.username());
        }
        if (req.email() != null && !req.email().equals(u.getEmail())) {
            if (repo.existsByEmail(req.email())) {
                throw new ConflictException("Email already registered: " + req.email());
            }
            u.setEmail(req.email());
        }
        if (req.role() != null) {
            u.setRole(req.role());
        }
        if (req.balance() != null) {
            u.setBalance(req.balance());
        }
        return mapper.toResponse(u);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw NotFoundException.of("User", id);
        }
        repo.deleteById(id);
    }

    public UserEntity find(UUID id) {
        return repo.findById(id).orElseThrow(() -> NotFoundException.of("User", id));
    }
}
