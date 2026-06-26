package com.gulshan.hiresenseai.repository;

import com.gulshan.hiresenseai.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    Optional<InterviewSession> findBySessionId(String sessionId);

    List<InterviewSession> findByUserIdOrderByStartTimeDesc(Long userId);

    List<InterviewSession> findAllByOrderByOverallScoreDesc();

    List<InterviewSession> findByJobRoleOrderByOverallScoreDesc(String jobRole);

    @Query("SELECT s FROM InterviewSession s ORDER BY s.overallScore DESC")
    List<InterviewSession> findTopCandidates();

    @Query("SELECT s FROM InterviewSession s WHERE s.user.id = :userId ORDER BY s.startTime DESC")
    List<InterviewSession> findByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);
}
