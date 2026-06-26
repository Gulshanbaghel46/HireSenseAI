package com.gulshan.hiresenseai.repository;

import com.gulshan.hiresenseai.entity.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {

    List<EmotionLog> findBySessionIdOrderByTimestamp(String sessionId);

    @Query("SELECT e.emotion, COUNT(e) FROM EmotionLog e WHERE e.sessionId = :sessionId GROUP BY e.emotion")
    List<Object[]> countEmotionsBySession(@Param("sessionId") String sessionId);

    @Query("SELECT AVG(CASE WHEN e.eyeContact = true THEN 100.0 ELSE 0.0 END) FROM EmotionLog e WHERE e.sessionId = :sessionId")
    Double avgEyeContactScore(@Param("sessionId") String sessionId);

    void deleteBySessionId(String sessionId);
}
