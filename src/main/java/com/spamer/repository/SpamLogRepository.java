package com.spamer.repository;

import com.spamer.domain.SpamLogEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpamLogRepository extends JpaRepository<SpamLogEntity, Long> {

    List<SpamLogEntity> findTop100ByCampaignIdOrderByCreatedAtDesc(Long campaignId);
}
