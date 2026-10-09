package com.example.spamer.domain.repository;

import com.example.spamer.domain.entity.SubscriptionEntity;
import com.example.spamer.domain.entity.SubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, SubscriptionId> {
}
