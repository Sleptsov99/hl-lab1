package com.spamer.repository;

import com.spamer.domain.ProxyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyRepository extends JpaRepository<ProxyEntity, Long> {

    List<ProxyEntity> findByEnabledTrue();
}
