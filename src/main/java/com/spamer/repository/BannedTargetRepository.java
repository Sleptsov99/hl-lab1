package com.spamer.repository;

import com.spamer.domain.BannedTargetEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannedTargetRepository extends JpaRepository<BannedTargetEntity, Long> {

    Optional<BannedTargetEntity> findByTarget(String target);

    boolean existsByTarget(String target);
}
