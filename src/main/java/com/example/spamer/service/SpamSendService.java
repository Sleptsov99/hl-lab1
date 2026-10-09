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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional endpoint #1. Charge + journal + the actual HTTP send happen in
 * one unit of work. Steps: find user and service, compute cost, check balance,
 * debit, journal (QUEUED), POST each message to the configured receiver, flip to
 * SENT. If the receiver rejects or is unreachable we throw, so the whole thing
 * rolls back and the user isn't charged for messages that never landed.
 *
 * The destination is operator config only (spamer.sender.target-url) - the
 * request body never chooses where this connects, so it can only reach the one
 * test receiver you point it at.
 *
 * Caveat worth knowing: an HTTP POST already accepted can't be "unsent" on
 * rollback. If message 1 of N succeeds and message 2 fails, the debit rolls back
 * but message 1 was still delivered to your receiver. For a test box that's fine;
 * in production you'd move the I/O out of the transaction and reconcile.
 */
@Service
public class SpamSendService {

    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final SpamLogRepository logRepo;
    private final ProxyProviderService proxyProvider;
    private final MessageSender sender;

    @Value("${spamer.sender.target-url}")
    private String target;

    public SpamSendService(
            UserRepository userRepo,
            ServiceRepository serviceRepo,
            SpamLogRepository logRepo,
            ProxyProviderService proxyProvider,
            MessageSender sender) {
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.logRepo = logRepo;
        this.proxyProvider = proxyProvider;
        this.sender = sender;
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

        user.setBalance(user.getBalance().subtract(cost));

        SpamLogEntity log = new SpamLogEntity();
        log.setUser(user);
        log.setVictimContact(req.victimContact());
        log.setMessageBody(req.messageBody());
        log.setStatus(SpamStatus.QUEUED);

        List<ProxyEntity> proxies = proxyProvider.pick(req.messageCount());
        log.setProxies(new HashSet<>(proxies));

        // real HTTP egress to the configured receiver
        int delivered = 0;
        int lastStatus = 0;
        for (int i = 1; i <= req.messageCount(); i++) {
            MessageSender.SendResult result =
                    sender.send(req.victimContact(), req.messageBody(), i);
            lastStatus = result.httpStatus();
            if (!result.ok()) {
                // rolls back the debit and the (unflushed) log row
                throw new BusinessException("Receiver rejected message " + i
                        + ": HTTP " + result.httpStatus()
                        + (result.responseSnippet() != null
                            ? " " + result.responseSnippet() : ""));
            }
            delivered++;
        }

        log.setStatus(SpamStatus.SENT);
        log.setSentAt(Instant.now());
        logRepo.save(log);

        return new SpamSendResponse(
                log.getId(), log.getStatus(), cost, user.getBalance(),
                delivered, target, lastStatus);
    }
}
