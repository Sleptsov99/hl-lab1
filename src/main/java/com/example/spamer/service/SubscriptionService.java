package com.example.spamer.service;

import com.example.spamer.domain.entity.ServiceEntity;
import com.example.spamer.domain.entity.SubscriptionEntity;
import com.example.spamer.domain.entity.SubscriptionId;
import com.example.spamer.domain.entity.UserEntity;
import com.example.spamer.domain.entity.UserRole;
import com.example.spamer.domain.repository.ServiceRepository;
import com.example.spamer.domain.repository.SubscriptionRepository;
import com.example.spamer.domain.repository.UserRepository;
import com.example.spamer.dto.request.SubscriptionRequest;
import com.example.spamer.dto.response.SubscriptionResponse;
import com.example.spamer.exception.ConflictException;
import com.example.spamer.exception.NotFoundException;
import com.example.spamer.mapper.SubscriptionMapper;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional endpoint #2. Creating the M2M subscription row and flipping the
 * user to USER_PRO have to be consistent - otherwise you get "PRO without a
 * subscription" or "subscription but still free". Both happen in one transaction.
 */
@Service
public class SubscriptionService {

    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final SubscriptionRepository subRepo;
    private final SubscriptionMapper mapper;

    public SubscriptionService(
            UserRepository userRepo,
            ServiceRepository serviceRepo,
            SubscriptionRepository subRepo,
            SubscriptionMapper mapper) {
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.subRepo = subRepo;
        this.mapper = mapper;
    }

    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest req) {
        UserEntity user = userRepo.findById(req.userId())
                .orElseThrow(() -> NotFoundException.of("User", req.userId()));
        ServiceEntity service = serviceRepo.findById(req.serviceId())
                .orElseThrow(() -> NotFoundException.of("Service", req.serviceId()));

        if (subRepo.existsForUserAndService(user.getId(), service.getId())) {
            throw new ConflictException("Already subscribed to service: " + service.getName());
        }

        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setId(new SubscriptionId(user.getId(), service.getId()));
        sub.setUser(user);
        sub.setService(service);
        // Set explicitly: with an assigned composite id save() goes through merge,
        // so @PrePersist wouldn't fire to stamp this.
        sub.setSubscribedAt(Instant.now());
        sub.setDiscountPercent(
                req.discountPercent() != null ? req.discountPercent() : BigDecimal.ZERO);
        subRepo.save(sub);

        // First paid subscription promotes a free user to PRO. Admins/operators keep their role.
        if (user.getRole() == UserRole.USER_FREE) {
            user.setRole(UserRole.USER_PRO);
        }

        return mapper.toResponse(sub);
    }
}
