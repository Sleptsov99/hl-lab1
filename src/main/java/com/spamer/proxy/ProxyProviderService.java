package com.spamer.proxy;

import com.spamer.domain.ProxyEntity;
import com.spamer.repository.ProxyRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProxyProviderService {

    private final ProxyRepository proxyRepository;
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    public ProxyProviderService(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
    }

    @Transactional(readOnly = true)
    public List<ProxyEntity> listEnabled() {
        return proxyRepository.findByEnabledTrue();
    }

    @Transactional(readOnly = true)
    public ProxyEntity selectNext() {
        List<ProxyEntity> proxies = listEnabled();
        if (proxies.isEmpty()) {
            return null;
        }
        int index = Math.floorMod(roundRobin.getAndIncrement(), proxies.size());
        return proxies.get(index);
    }

    @Transactional
    public ProxyEntity create(String host, int port, String username, String password) {
        ProxyEntity proxy = new ProxyEntity();
        proxy.setHost(host);
        proxy.setPort(port);
        proxy.setUsername(username);
        proxy.setPassword(password);
        return proxyRepository.save(proxy);
    }
}
