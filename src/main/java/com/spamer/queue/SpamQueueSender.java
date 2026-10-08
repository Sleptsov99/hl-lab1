package com.spamer.queue;

import com.spamer.domain.CampaignStatus;
import com.spamer.domain.ProxyEntity;
import com.spamer.domain.ServiceEntity;
import com.spamer.domain.SpamCampaignEntity;
import com.spamer.outer.OuterApiClient;
import com.spamer.outer.OuterApiResult;
import com.spamer.proxy.ProxyProviderService;
import com.spamer.repository.ServiceRepository;
import com.spamer.repository.SpamCampaignRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SpamQueueSender {

    private static final Logger log = LoggerFactory.getLogger(SpamQueueSender.class);

    private final SpamCampaignRepository campaignRepository;
    private final ServiceRepository serviceRepository;
    private final OuterApiClient outerApiClient;
    private final ProxyProviderService proxyProviderService;
    private final SpamQueuePersistence persistence;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<Long, AtomicBoolean> stopFlags = new ConcurrentHashMap<>();

    public SpamQueueSender(
            SpamCampaignRepository campaignRepository,
            ServiceRepository serviceRepository,
            OuterApiClient outerApiClient,
            ProxyProviderService proxyProviderService,
            SpamQueuePersistence persistence) {
        this.campaignRepository = campaignRepository;
        this.serviceRepository = serviceRepository;
        this.outerApiClient = outerApiClient;
        this.proxyProviderService = proxyProviderService;
        this.persistence = persistence;
    }

    public void enqueue(Long campaignId) {
        stopFlags.put(campaignId, new AtomicBoolean(false));
        executor.submit(() -> runCampaign(campaignId));
    }

    public void stop(Long campaignId) {
        AtomicBoolean flag = stopFlags.get(campaignId);
        if (flag != null) {
            flag.set(true);
        }
    }

    protected void runCampaign(Long campaignId) {
        SpamCampaignEntity campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            log.warn("Campaign {} not found, skipping queue run", campaignId);
            return;
        }

        ServiceEntity service = serviceRepository.findById(campaign.getServiceId()).orElse(null);
        if (service == null) {
            persistence.markFinished(campaignId, CampaignStatus.FAILED);
            return;
        }

        persistence.markRunning(campaignId);

        final SpamCampaignEntity activeCampaign = campaign;
        final ServiceEntity activeService = service;

        AtomicBoolean stopFlag = stopFlags.getOrDefault(campaignId, new AtomicBoolean(false));
        Semaphore concurrencyLimit = new Semaphore(campaign.getConcurrency());
        AtomicInteger sent = new AtomicInteger(0);
        long delayMs = campaign.getRatePerSecond() > 0
                ? 1000L / campaign.getRatePerSecond()
                : 100L;

        try {
            while (sent.get() < campaign.getTotalRequests() && !stopFlag.get()) {
                concurrencyLimit.acquire();
                sent.incrementAndGet();

                executor.submit(() -> {
                    try {
                        ProxyEntity proxy = proxyProviderService.selectNext();
                        OuterApiResult result = outerApiClient.send(
                                activeService, activeCampaign.getTargetEmail(), proxy);
                        persistence.persistResult(activeCampaign, activeService, result);
                    } catch (Exception ex) {
                        log.warn("Request failed for campaign {}: {}", campaignId, ex.getMessage());
                        persistence.persistResult(
                                activeCampaign,
                                activeService,
                                new OuterApiResult(0, 0L, ex.getMessage()));
                    } finally {
                        concurrencyLimit.release();
                    }
                });

                if (delayMs > 0) {
                    TimeUnit.MILLISECONDS.sleep(delayMs);
                }
            }

            concurrencyLimit.acquire(campaign.getConcurrency());
            concurrencyLimit.release(campaign.getConcurrency());

            CampaignStatus finalStatus = stopFlag.get()
                    ? CampaignStatus.STOPPED
                    : CampaignStatus.COMPLETED;
            persistence.markFinished(campaignId, finalStatus);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            persistence.markFinished(campaignId, CampaignStatus.FAILED);
        } finally {
            stopFlags.remove(campaignId);
        }
    }
}
