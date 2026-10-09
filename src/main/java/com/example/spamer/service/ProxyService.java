package com.example.spamer.service;

import com.example.spamer.domain.entity.ProxyEntity;
import com.example.spamer.domain.entity.ProxyStatus;
import com.example.spamer.domain.entity.ServiceEntity;
import com.example.spamer.domain.repository.ProxyRepository;
import com.example.spamer.dto.request.CreateProxyRequest;
import com.example.spamer.dto.request.PatchProxyRequest;
import com.example.spamer.dto.response.ProxyResponse;
import com.example.spamer.exception.NotFoundException;
import com.example.spamer.mapper.ProxyMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProxyService {

    private final ProxyRepository repo;
    private final ServiceCatalogService services;
    private final ProxyMapper mapper;

    @Transactional
    public ProxyResponse create(CreateProxyRequest req) {
        ProxyEntity p = new ProxyEntity();
        p.setHost(req.host());
        p.setPort(req.port());
        p.setProtocol(req.protocol());
        p.setCountry(req.country());
        p.setStatus(req.status());
        p.setProvider(resolveProvider(req.providerId()));
        return mapper.toResponse(repo.save(p));
    }

    @Transactional(readOnly = true)
    public ProxyResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<ProxyResponse> list(int page, int size) {
        return repo.findAll(PageRequest.of(page, size)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return repo.count();
    }

    @Transactional
    public ProxyResponse replace(UUID id, CreateProxyRequest req) {
        ProxyEntity p = find(id);
        p.setHost(req.host());
        p.setPort(req.port());
        p.setProtocol(req.protocol());
        p.setCountry(req.country());
        p.setStatus(req.status());
        p.setProvider(resolveProvider(req.providerId()));
        return mapper.toResponse(p);
    }

    @Transactional
    public ProxyResponse patch(UUID id, PatchProxyRequest req) {
        ProxyEntity p = find(id);
        if (req.host() != null) {
            p.setHost(req.host());
        }
        if (req.port() != null) {
            p.setPort(req.port());
        }
        if (req.protocol() != null) {
            p.setProtocol(req.protocol());
        }
        if (req.country() != null) {
            p.setCountry(req.country());
        }
        if (req.status() != null) {
            p.setStatus(req.status());
        }
        if (req.providerId() != null) {
            p.setProvider(resolveProvider(req.providerId()));
        }
        return mapper.toResponse(p);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw NotFoundException.of("Proxy", id);
        }
        repo.deleteById(id);
    }

    ProxyEntity find(UUID id) {
        return repo.findById(id).orElseThrow(() -> NotFoundException.of("Proxy", id));
    }

    @Transactional(readOnly = true)
    public List<ProxyEntity> findActive(int limit) {
        return repo.findByStatus(ProxyStatus.ACTIVE, Limit.of(limit));
    }

    private ServiceEntity resolveProvider(UUID providerId) {
        if (providerId == null) {
            return null;
        }
        return services.find(providerId);
    }
}
