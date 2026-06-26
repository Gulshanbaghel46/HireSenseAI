package com.gulshan.hiresenseai.repository;

import com.gulshan.hiresenseai.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    List<InterviewAnswer> findBySessionId(String sessionId);

    @Query("SELECT DISTINCT i.sessionId FROM InterviewAnswer i")
    List<String> findAllSessionIds();

    @Query("SELECT DISTINCT i.sessionId FROM InterviewAnswer i WHERE i.sessionId IN " +
           "(SELECT s.sessionId FROM InterviewSession s WHERE s.user.id = :userId)")
    List<String> findSessionIdsByUserId(Long userId);

    void deleteBySessionId(String sessionId);
}