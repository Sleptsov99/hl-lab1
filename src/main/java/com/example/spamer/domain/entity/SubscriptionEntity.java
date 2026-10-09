package com.example.spamer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_service_subscriptions")
public class SubscriptionEntity {

    @EmbeddedId
    private SubscriptionId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserEntity user;

    @MapsId("serviceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ServiceEntity service;

    @Column(nullable = false)
    private Instant subscribedAt;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @PrePersist
    void onCreate() {
        if (subscribedAt == null) {
            subscribedAt = Instant.now();
        }
    }
}
