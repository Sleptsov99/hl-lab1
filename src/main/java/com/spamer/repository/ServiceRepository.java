package com.spamer.repository;

import com.spamer.domain.ServiceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findByEnabledTrue();

    Optional<ServiceEntity> findByName(String name);
}
