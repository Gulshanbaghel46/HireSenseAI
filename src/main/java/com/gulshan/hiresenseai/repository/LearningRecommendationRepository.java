package com.gulshan.hiresenseai.repository;

import com.gulshan.hiresenseai.entity.LearningRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LearningRecommendationRepository extends JpaRepository<LearningRecommendation, Long> {
    List<LearningRecommendation> findBySessionIdOrderByPriority(String sessionId);
    void deleteBySessionId(String sessionId);
}
