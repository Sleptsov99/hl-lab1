package com.example.spamer.domain.repository;

import com.example.spamer.domain.entity.SpamLogEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpamLogRepository extends JpaRepository<SpamLogEntity, UUID> {

    // Cursor feed: keyset over the UUID pk. Not chronological, but stable and
    // index-friendly, which is all infinite-scroll needs here.
    @Query("select s from SpamLogEntity s where s.id > :cursor order by s.id asc")
    List<SpamLogEntity> findAfterCursor(@Param("cursor") UUID cursor, Limit limit);

    @Query("select s from SpamLogEntity s order by s.id asc")
    List<SpamLogEntity> findFirstPage(Limit limit);
}
