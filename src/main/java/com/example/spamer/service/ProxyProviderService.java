package com.example.spamer.service;

import com.example.spamer.domain.entity.ProxyEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stand-in for the "proxy provider" box in the architecture sketch. In the real
 * design this would call an outer API; here it asks ProxyService for ACTIVE rows.
 * No network, nothing to talk to - it only exists so /spam/send has something to
 * attach as the M2M link.
 */
@Service
@RequiredArgsConstructor
public class ProxyProviderService {

    private final ProxyService proxies;

    @Transactional(readOnly = true)
    public List<ProxyEntity> pick(int max) {
        return proxies.findActive(Math.max(1, max));
    }
}
