package com.example.spamer.domain.repository;

import com.example.spamer.domain.entity.SubscriptionEntity;
import com.example.spamer.domain.entity.SubscriptionId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, SubscriptionId> {

    @Query("select count(s) > 0 from SubscriptionEntity s "
            + "where s.id.userId = :userId and s.id.serviceId = :serviceId")
    boolean existsForUserAndService(@Param("userId") UUID userId, @Param("serviceId") UUID serviceId);
}
