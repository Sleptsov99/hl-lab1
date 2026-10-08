package com.example.spamer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.spamer.domain.entity.ServiceEntity;
import com.example.spamer.domain.entity.SubscriptionEntity;
import com.example.spamer.domain.entity.UserEntity;
import com.example.spamer.domain.entity.UserRole;
import com.example.spamer.domain.repository.ServiceRepository;
import com.example.spamer.domain.repository.SubscriptionRepository;
import com.example.spamer.domain.repository.UserRepository;
import com.example.spamer.dto.request.SubscriptionRequest;
import com.example.spamer.dto.response.SubscriptionResponse;
import com.example.spamer.exception.ConflictException;
import com.example.spamer.mapper.SubscriptionMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock UserRepository userRepo;
    @Mock ServiceRepository serviceRepo;
    @Mock SubscriptionRepository subRepo;
    @Mock SubscriptionMapper mapper;

    SubscriptionService service;

    UUID userId = UUID.randomUUID();
    UUID serviceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(userRepo, serviceRepo, subRepo, mapper);
    }

    private void stubMapper() {
        when(mapper.toResponse(any())).thenReturn(
                new SubscriptionResponse(userId, serviceId, Instant.now(), BigDecimal.TEN));
    }

    private UserEntity user(UserRole role) {
        UserEntity u = new UserEntity();
        u.setId(userId);
        u.setRole(role);
        return u;
    }

    private ServiceEntity svc() {
        ServiceEntity s = new ServiceEntity();
        s.setId(serviceId);
        s.setName("svc");
        return s;
    }

    @Test
    void freeUserBecomesPro() {
        UserEntity u = user(UserRole.USER_FREE);
        when(userRepo.findById(userId)).thenReturn(Optional.of(u));
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(svc()));
        when(subRepo.existsForUserAndService(userId, serviceId)).thenReturn(false);
        stubMapper();

        service.subscribe(new SubscriptionRequest(userId, serviceId, new BigDecimal("10.00")));

        assertThat(u.getRole()).isEqualTo(UserRole.USER_PRO);
        verify(subRepo).save(any(SubscriptionEntity.class));
    }

    @Test
    void adminKeepsRole() {
        UserEntity u = user(UserRole.ADMIN);
        when(userRepo.findById(userId)).thenReturn(Optional.of(u));
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(svc()));
        when(subRepo.existsForUserAndService(userId, serviceId)).thenReturn(false);
        stubMapper();

        service.subscribe(new SubscriptionRequest(userId, serviceId, null));

        assertThat(u.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void duplicateRejected() {
        when(userRepo.findById(userId)).thenReturn(Optional.of(user(UserRole.USER_FREE)));
        when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(svc()));
        when(subRepo.existsForUserAndService(userId, serviceId)).thenReturn(true);

        assertThatThrownBy(() -> service.subscribe(
                new SubscriptionRequest(userId, serviceId, null)))
                .isInstanceOf(ConflictException.class);

        verify(subRepo, never()).save(any());
    }
}
