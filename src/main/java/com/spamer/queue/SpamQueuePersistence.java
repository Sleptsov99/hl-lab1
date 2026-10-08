package com.spamer.queue;

import com.spamer.domain.CampaignStatus;
import com.spamer.domain.ServiceEntity;
import com.spamer.domain.SpamCampaignEntity;
import com.spamer.domain.SpamLogEntity;
import com.spamer.outer.OuterApiResult;
import com.spamer.repository.SpamCampaignRepository;
import com.spamer.repository.SpamLogRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SpamQueuePersistence {

    private final SpamCampaignRepository campaignRepository;
    private final SpamLogRepository spamLogRepository;

    public SpamQueuePersistence(
            SpamCampaignRepository campaignRepository,
            SpamLogRepository spamLogRepository) {
        this.campaignRepository = campaignRepository;
        this.spamLogRepository = spamLogRepository;
    }

    @Transactional
    public void markRunning(Long campaignId) {
        SpamCampaignEntity campaign = campaignRepository.findById(campaignId).orElseThrow();
        campaign.setStatus(CampaignStatus.RUNNING);
        campaign.setStartedAt(Instant.now());
        campaignRepository.save(campaign);
    }

    @Transactional
    public void markFinished(Long campaignId, CampaignStatus status) {
        SpamCampaignEntity campaign = campaignRepository.findById(campaignId).orElseThrow();
        campaign.setStatus(status);
        campaign.setStoppedAt(Instant.now());
        campaignRepository.save(campaign);
    }

    @Transactional
    public void persistResult(SpamCampaignEntity campaign, ServiceEntity service, OuterApiResult result) {
        SpamLogEntity logEntry = new SpamLogEntity();
        logEntry.setCampaignId(campaign.getId());
        logEntry.setServiceId(service.getId());
        logEntry.setUserId(campaign.getUserId());
        logEntry.setStatusCode(result.statusCode());
        logEntry.setResponseTimeMs(result.responseTimeMs());
        logEntry.setErrorMessage(result.errorMessage());
        spamLogRepository.save(logEntry);

        SpamCampaignEntity fresh = campaignRepository.findById(campaign.getId()).orElse(campaign);
        if (result.isSuccess()) {
            fresh.setSuccessCount(fresh.getSuccessCount() + 1);
        } else {
            fresh.setFailCount(fresh.getFailCount() + 1);
        }
        campaignRepository.save(fresh);
    }
}
