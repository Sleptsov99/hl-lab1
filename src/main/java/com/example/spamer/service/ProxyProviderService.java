package com.example.spamer.service;

import com.example.spamer.domain.entity.ProxyEntity;
import com.example.spamer.domain.entity.ProxyStatus;
import com.example.spamer.domain.repository.ProxyRepository;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stand-in for the "proxy provider" box in the architecture sketch. In the real
 * design this would call an outer API; here it's a pure in-process mock that
 * just hands back ACTIVE proxy rows from our own table. No network, nothing to
 * talk to - it only exists so /spam/send has something to attach as the M2M link.
 */
@Service
public class ProxyProviderService {

    private final ProxyRepository repo;

    public ProxyProviderService(ProxyRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ProxyEntity> pick(int max) {
        return repo.findByStatus(ProxyStatus.ACTIVE, Limit.of(Math.max(1, max)));
    }
}
