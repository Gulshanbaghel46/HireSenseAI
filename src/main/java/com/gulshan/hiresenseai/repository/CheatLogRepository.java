package com.gulshan.hiresenseai.repository;

import com.gulshan.hiresenseai.entity.CheatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CheatLogRepository extends JpaRepository<CheatLog, Long> {
    List<CheatLog> findBySessionIdOrderByTimestamp(String sessionId);
    long countBySessionId(String sessionId);
    void deleteBySessionId(String sessionId);
}
