package com.spamer.queue;

import com.spamer.domain.CampaignStatus;
import com.spamer.repository.SpamCampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PendingCampaignRecovery {

    private static final Logger log = LoggerFactory.getLogger(PendingCampaignRecovery.class);

    private final SpamCampaignRepository campaignRepository;
    private final SpamQueueSender spamQueueSender;

    public PendingCampaignRecovery(
            SpamCampaignRepository campaignRepository,
            SpamQueueSender spamQueueSender) {
        this.campaignRepository = campaignRepository;
        this.spamQueueSender = spamQueueSender;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recoverPendingCampaigns() {
        campaignRepository.findByStatus(CampaignStatus.PENDING).forEach(campaign -> {
            log.info("Re-enqueueing stuck PENDING campaign {}", campaign.getId());
            spamQueueSender.enqueue(campaign.getId());
        });
    }
}
