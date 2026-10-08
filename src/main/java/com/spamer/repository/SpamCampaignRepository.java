package com.spamer.repository;

import com.spamer.domain.CampaignStatus;
import com.spamer.domain.SpamCampaignEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpamCampaignRepository extends JpaRepository<SpamCampaignEntity, Long> {

    List<SpamCampaignEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SpamCampaignEntity> findAllByOrderByCreatedAtDesc();

    List<SpamCampaignEntity> findByStatus(CampaignStatus status);
}
