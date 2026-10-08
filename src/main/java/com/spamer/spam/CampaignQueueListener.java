package com.spamer.spam;

import com.spamer.queue.SpamQueueSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CampaignQueueListener {

    private final SpamQueueSender spamQueueSender;

    public CampaignQueueListener(SpamQueueSender spamQueueSender) {
        this.spamQueueSender = spamQueueSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCampaignCreated(CampaignCreatedEvent event) {
        spamQueueSender.enqueue(event.campaignId());
    }
}
