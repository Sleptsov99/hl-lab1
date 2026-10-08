package com.example.spamer.domain.repository;

import com.example.spamer.domain.entity.ServiceEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {

    boolean existsByName(String name);
}
