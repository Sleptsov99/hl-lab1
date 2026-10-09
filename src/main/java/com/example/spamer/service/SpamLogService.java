package com.example.spamer.service;

import com.example.spamer.domain.entity.SpamLogEntity;
import com.example.spamer.domain.repository.SpamLogRepository;
import com.example.spamer.dto.request.CreateSpamLogRequest;
import com.example.spamer.dto.request.PatchSpamLogRequest;
import com.example.spamer.dto.response.CursorPage;
import com.example.spamer.dto.response.SpamLogResponse;
import com.example.spamer.exception.NotFoundException;
import com.example.spamer.mapper.SpamLogMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpamLogService {

    private final SpamLogRepository repo;
    private final UserService users;
    private final SpamLogMapper mapper;

    @Transactional
    public SpamLogResponse create(CreateSpamLogRequest req) {
        SpamLogEntity log = new SpamLogEntity();
        log.setUser(users.find(req.userId()));
        log.setVictimContact(req.victimContact());
        log.setMessageBody(req.messageBody());
        log.setStatus(req.status());
        return mapper.toResponse(repo.save(log));
    }

    @Transactional(readOnly = true)
    public SpamLogResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<SpamLogResponse> list(int page, int size) {
        return repo.findAll(PageRequest.of(page, size)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return repo.count();
    }

    // Cursor feed for infinite scroll. We fetch size+1 to know if there's more
    // without doing a separate count query.
    @Transactional(readOnly = true)
    public CursorPage<SpamLogResponse> feed(UUID cursor, int size) {
        List<SpamLogEntity> rows = cursor == null
                ? repo.findAllByOrderByIdAsc(Limit.of(size + 1))
                : repo.findByIdGreaterThanOrderByIdAsc(cursor, Limit.of(size + 1));

        String next = null;
        if (rows.size() > size) {
            SpamLogEntity last = rows.get(size - 1);
            next = last.getId().toString();
            rows = rows.subList(0, size);
        }
        List<SpamLogResponse> items = rows.stream().map(mapper::toResponse).toList();
        return new CursorPage<>(items, next);
    }

    @Transactional
    public SpamLogResponse replace(UUID id, CreateSpamLogRequest req) {
        SpamLogEntity log = find(id);
        log.setUser(users.find(req.userId()));
        log.setVictimContact(req.victimContact());
        log.setMessageBody(req.messageBody());
        log.setStatus(req.status());
        return mapper.toResponse(log);
    }

    @Transactional
    public SpamLogResponse patch(UUID id, PatchSpamLogRequest req) {
        SpamLogEntity log = find(id);
        if (req.victimContact() != null) {
            log.setVictimContact(req.victimContact());
        }
        if (req.messageBody() != null) {
            log.setMessageBody(req.messageBody());
        }
        if (req.status() != null) {
            log.setStatus(req.status());
        }
        return mapper.toResponse(log);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw NotFoundException.of("SpamLog", id);
        }
        repo.deleteById(id);
    }

    @Transactional
    public SpamLogEntity save(SpamLogEntity log) {
        return repo.save(log);
    }

    SpamLogEntity find(UUID id) {
        return repo.findById(id).orElseThrow(() -> NotFoundException.of("SpamLog", id));
    }
}
