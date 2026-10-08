package com.spamer.spam;

import com.spamer.ban.BanService;
import com.spamer.domain.CampaignStatus;
import com.spamer.domain.SpamCampaignEntity;
import com.spamer.domain.UserEntity;
import com.spamer.domain.UserTier;
import com.spamer.payment.PayService;
import com.spamer.queue.SpamQueueSender;
import com.spamer.repository.ServiceRepository;
import com.spamer.repository.SpamCampaignRepository;
import com.spamer.repository.SpamLogRepository;
import com.spamer.repository.UserRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpamService {

    private final SpamCampaignRepository campaignRepository;
    private final ServiceRepository serviceRepository;
    private final SpamLogRepository spamLogRepository;
    private final UserRepository userRepository;
    private final PayService payService;
    private final BanService banService;
    private final ApplicationEventPublisher events;
    private final SpamQueueSender spamQueueSender;

    public SpamService(
            SpamCampaignRepository campaignRepository,
            ServiceRepository serviceRepository,
            SpamLogRepository spamLogRepository,
            UserRepository userRepository,
            PayService payService,
            BanService banService,
            ApplicationEventPublisher events,
            SpamQueueSender spamQueueSender) {
        this.campaignRepository = campaignRepository;
        this.serviceRepository = serviceRepository;
        this.spamLogRepository = spamLogRepository;
        this.userRepository = userRepository;
        this.payService = payService;
        this.banService = banService;
        this.events = events;
        this.spamQueueSender = spamQueueSender;
    }

    @Transactional
    public SpamCampaignEntity createCampaign(
            String username,
            String name,
            Long serviceId,
            String targetEmail,
            int totalRequests,
            int ratePerSecond,
            int concurrency) {

        banService.ensureNotBanned(targetEmail);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        serviceRepository.findById(serviceId)
                .filter(s -> s.isEnabled())
                .orElseThrow(() -> new IllegalArgumentException("Service not found or disabled"));

        payService.validateCampaignLimits(user.getTier(), ratePerSecond, concurrency, totalRequests);

        SpamCampaignEntity campaign = new SpamCampaignEntity();
        campaign.setName(name);
        campaign.setServiceId(serviceId);
        campaign.setUserId(user.getId());
        campaign.setTargetEmail(targetEmail);
        campaign.setTotalRequests(totalRequests);
        campaign.setRatePerSecond(ratePerSecond);
        campaign.setConcurrency(concurrency);
        campaign.setStatus(CampaignStatus.PENDING);

        campaign = campaignRepository.save(campaign);
        events.publishEvent(new CampaignCreatedEvent(campaign.getId()));
        return campaign;
    }

    @Transactional(readOnly = true)
    public List<SpamCampaignEntity> listCampaigns(String username, boolean admin) {
        if (admin) {
            return campaignRepository.findAllByOrderByCreatedAtDesc();
        }
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return campaignRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public SpamCampaignEntity stopCampaign(Long id, String username, boolean admin) {
        SpamCampaignEntity campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (!admin) {
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!campaign.getUserId().equals(user.getId())) {
                throw new IllegalArgumentException("Access denied");
            }
        }

        spamQueueSender.stop(id);
        campaign.setStatus(CampaignStatus.STOPPED);
        return campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public UserTier getUserTier(String username) {
        return userRepository.findByUsername(username)
                .map(UserEntity::getTier)
                .orElse(UserTier.FREE);
    }

    @Transactional(readOnly = true)
    public List<?> getCampaignLogs(Long campaignId) {
        return spamLogRepository.findTop100ByCampaignIdOrderByCreatedAtDesc(campaignId);
    }
}
