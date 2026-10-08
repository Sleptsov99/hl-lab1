package com.example.spamer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SubscriptionId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "service_id")
    private UUID serviceId;

    public SubscriptionId() {
    }

    public SubscriptionId(UUID userId, UUID serviceId) {
        this.userId = userId;
        this.serviceId = serviceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscriptionId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, serviceId);
    }
}
