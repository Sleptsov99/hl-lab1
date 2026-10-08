package com.example.spamer.service;

import com.example.spamer.domain.entity.ProxyEntity;
import com.example.spamer.domain.entity.ServiceEntity;
import com.example.spamer.domain.entity.SpamLogEntity;
import com.example.spamer.domain.entity.SpamStatus;
import com.example.spamer.domain.entity.UserEntity;
import com.example.spamer.domain.repository.ServiceRepository;
import com.example.spamer.domain.repository.SpamLogRepository;
import com.example.spamer.domain.repository.UserRepository;
import com.example.spamer.dto.request.SpamSendRequest;
import com.example.spamer.dto.response.SpamSendResponse;
import com.example.spamer.exception.BusinessException;
import com.example.spamer.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional endpoint #1. Charge + journal in one unit of work: if any step
 * blows up we must not leave money debited with no log row, or a log row the
 * user didn't pay for. So the whole thing is one transaction - partial state here
 * means lost money or ghost records.
 *
 * Nothing actually leaves the process: "sending" is just flipping the log row to
 * SENT. The proxy attach is the M2M link required by the lab, not a real route.
 */
@Service
public class SpamSendService {

    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final SpamLogRepository logRepo;
    private final ProxyProviderService proxyProvider;

    public SpamSendService(
            UserRepository userRepo,
            ServiceRepository serviceRepo,
            SpamLogRepository logRepo,
            ProxyProviderService proxyProvider) {
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.logRepo = logRepo;
        this.proxyProvider = proxyProvider;
    }

    @Transactional
    public SpamSendResponse send(SpamSendRequest req) {
        UserEntity user = userRepo.findById(req.userId())
                .orElseThrow(() -> NotFoundException.of("User", req.userId()));
        ServiceEntity service = serviceRepo.findById(req.serviceId())
                .orElseThrow(() -> NotFoundException.of("Service", req.serviceId()));

        if (!service.isActive()) {
            throw new BusinessException("Service is not active: " + service.getName());
        }

        BigDecimal cost = service.getPricePerMessage()
                .multiply(BigDecimal.valueOf(req.messageCount()));

        if (user.getBalance().compareTo(cost) < 0) {
            throw new BusinessException("Insufficient balance: need " + cost
                    + ", have " + user.getBalance());
        }

        // debit first, then journal. Both roll back together if anything fails.
        user.setBalance(user.getBalance().subtract(cost));

        SpamLogEntity log = new SpamLogEntity();
        log.setUser(user);
        log.setVictimContact(req.victimContact());
        log.setMessageBody(req.messageBody());
        log.setStatus(SpamStatus.QUEUED);

        List<ProxyEntity> proxies = proxyProvider.pick(req.messageCount());
        log.setProxies(new HashSet<>(proxies));

        log.setStatus(SpamStatus.SENT);
        log.setSentAt(Instant.now());
        logRepo.save(log);

        return new SpamSendResponse(log.getId(), log.getStatus(), cost, user.getBalance());
    }
}
