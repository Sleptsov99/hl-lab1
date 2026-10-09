package com.example.spamer.service;

import com.example.spamer.domain.entity.ServiceEntity;
import com.example.spamer.domain.repository.ServiceRepository;
import com.example.spamer.dto.request.CreateServiceRequest;
import com.example.spamer.dto.request.PatchServiceRequest;
import com.example.spamer.dto.response.ServiceResponse;
import com.example.spamer.exception.ConflictException;
import com.example.spamer.exception.NotFoundException;
import com.example.spamer.mapper.ServiceMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceRepository repo;
    private final ServiceMapper mapper;

    @Transactional
    public ServiceResponse create(CreateServiceRequest req) {
        if (repo.existsByName(req.name())) {
            throw new ConflictException("Service name already exists: " + req.name());
        }
        ServiceEntity s = new ServiceEntity();
        apply(s, req.name(), req.description(), req);
        return mapper.toResponse(repo.save(s));
    }

    @Transactional(readOnly = true)
    public ServiceResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> list(int page, int size) {
        return repo.findAll(PageRequest.of(page, size)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return repo.count();
    }

    @Transactional
    public ServiceResponse replace(UUID id, CreateServiceRequest req) {
        ServiceEntity s = find(id);
        if (!s.getName().equals(req.name()) && repo.existsByName(req.name())) {
            throw new ConflictException("Service name already exists: " + req.name());
        }
        apply(s, req.name(), req.description(), req);
        return mapper.toResponse(s);
    }

    @Transactional
    public ServiceResponse patch(UUID id, PatchServiceRequest req) {
        ServiceEntity s = find(id);
        if (req.name() != null && !req.name().equals(s.getName())) {
            if (repo.existsByName(req.name())) {
                throw new ConflictException("Service name already exists: " + req.name());
            }
            s.setName(req.name());
        }
        if (req.description() != null) {
            s.setDescription(req.description());
        }
        if (req.pricePerMessage() != null) {
            s.setPricePerMessage(req.pricePerMessage());
        }
        if (req.active() != null) {
            s.setActive(req.active());
        }
        return mapper.toResponse(s);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw NotFoundException.of("Service", id);
        }
        repo.deleteById(id);
    }

    public ServiceEntity find(UUID id) {
        return repo.findById(id).orElseThrow(() -> NotFoundException.of("Service", id));
    }

    private void apply(ServiceEntity s, String name, String description, CreateServiceRequest req) {
        s.setName(name);
        s.setDescription(description);
        s.setPricePerMessage(req.pricePerMessage());
        s.setActive(req.active() == null || req.active());
    }
}
