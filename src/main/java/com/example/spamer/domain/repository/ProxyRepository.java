package com.example.spamer.domain.repository;

import com.example.spamer.domain.entity.ProxyEntity;
import com.example.spamer.domain.entity.ProxyStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyRepository extends JpaRepository<ProxyEntity, UUID> {

    List<ProxyEntity> findByStatus(ProxyStatus status, Limit limit);
}
