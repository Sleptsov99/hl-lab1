package com.example.spamer.domain.repository;

import com.example.spamer.domain.entity.SpamLogEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpamLogRepository extends JpaRepository<SpamLogEntity, UUID> {

    List<SpamLogEntity> findByIdGreaterThanOrderByIdAsc(UUID id, Limit limit);

    List<SpamLogEntity> findAllByOrderByIdAsc(Limit limit);
}
