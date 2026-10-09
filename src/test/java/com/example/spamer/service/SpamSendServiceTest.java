package com.example.spamer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.spamer.domain.entity.ProxyEntity;
import com.example.spamer.domain.entity.ServiceEntity;
import com.example.spamer.domain.entity.SpamLogEntity;
import com.example.spamer.domain.entity.SpamStatus;
import com.example.spamer.domain.entity.UserEntity;
import com.example.spamer.dto.request.SpamSendRequest;
import com.example.spamer.dto.response.SpamSendResponse;
import com.example.spamer.exception.BusinessException;
import com.example.spamer.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpamSendServiceTest {

    @Mock UserService users;
    @Mock ServiceCatalogService services;
    @Mock SpamLogService logs;
    @Mock ProxyProviderService proxyProvider;
    @Mock MessageSender sender;

    SpamSendService service;

    UUID userId = UUID.randomUUID();
    UUID serviceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SpamSendService(users, services, logs, proxyProvider, sender);
    }

    private UserEntity user(String balance) {
        UserEntity u = new UserEntity();
        u.setId(userId);
        u.setBalance(new BigDecimal(balance));
        return u;
    }

    private ServiceEntity catalogItem(String price, boolean active) {
        ServiceEntity s = new ServiceEntity();
        s.setId(serviceId);
        s.setName("svc");
        s.setPricePerMessage(new BigDecimal(price));
        s.setActive(active);
        return s;
    }

    @Test
    void chargesAndMarksSent() {
        when(users.find(userId)).thenReturn(user("100.00"));
        when(services.find(serviceId)).thenReturn(catalogItem("2.00", true));
        ProxyEntity proxy = new ProxyEntity();
        proxy.setId(UUID.randomUUID());
        when(proxyProvider.pick(2)).thenReturn(List.of(proxy));
        when(sender.send(any(), any(), anyInt()))
                .thenReturn(new MessageSender.SendResult(200, true, "{\"received\":true}", 3));

        SpamSendResponse res = service.send(
                new SpamSendRequest(userId, serviceId, "t@example.com", "hi", 2));

        assertThat(res.status()).isEqualTo(SpamStatus.SENT);
        assertThat(res.charged()).isEqualByComparingTo("4.00");
        assertThat(res.remainingBalance()).isEqualByComparingTo("96.00");
        assertThat(res.delivered()).isEqualTo(2);
        verify(sender, times(2)).send(any(), any(), anyInt());
        verify(logs).save(any(SpamLogEntity.class));
    }

    @Test
    void rollsBackWhenReceiverRejects() {
        when(users.find(userId)).thenReturn(user("100.00"));
        when(services.find(serviceId)).thenReturn(catalogItem("2.00", true));
        when(proxyProvider.pick(1)).thenReturn(List.of());
        when(sender.send(any(), any(), anyInt()))
                .thenReturn(new MessageSender.SendResult(503, false, "down", 1));

        assertThatThrownBy(() -> service.send(
                new SpamSendRequest(userId, serviceId, "t@example.com", null, 1)))
                .isInstanceOf(BusinessException.class);

        verify(logs, never()).save(any());
    }

    @Test
    void rejectsWhenBalanceTooLow() {
        when(users.find(userId)).thenReturn(user("1.00"));
        when(services.find(serviceId)).thenReturn(catalogItem("5.00", true));

        assertThatThrownBy(() -> service.send(
                new SpamSendRequest(userId, serviceId, "t@example.com", null, 1)))
                .isInstanceOf(BusinessException.class);

        verify(logs, never()).save(any());
    }

    @Test
    void rejectsInactiveService() {
        when(users.find(userId)).thenReturn(user("100.00"));
        when(services.find(serviceId)).thenReturn(catalogItem("1.00", false));

        assertThatThrownBy(() -> service.send(
                new SpamSendRequest(userId, serviceId, "t@example.com", null, 1)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void missingUserGives404() {
        when(users.find(userId)).thenThrow(NotFoundException.of("User", userId));

        assertThatThrownBy(() -> service.send(
                new SpamSendRequest(userId, serviceId, "t@example.com", null, 1)))
                .isInstanceOf(NotFoundException.class);
    }
}
