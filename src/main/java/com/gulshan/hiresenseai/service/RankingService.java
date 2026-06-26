package com.gulshan.hiresenseai.service;

import com.gulshan.hiresenseai.entity.InterviewSession;
import com.gulshan.hiresenseai.entity.LearningRecommendation;
import com.gulshan.hiresenseai.repository.InterviewSessionRepository;
import com.gulshan.hiresenseai.repository.LearningRecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RankingService {

    @Autowired private InterviewSessionRepository sessionRepository;
    @Autowired private LearningRecommendationRepository recommendationRepository;

    // ─── Composite score weights ───────────────────────────────────────────────
    private static final double W_INTERVIEW   = 0.50;
    private static final double W_RESUME      = 0.15;
    private static final double W_EMOTION     = 0.10;
    private static final double W_COMM        = 0.15;
    private static final double W_ATTENTION   = 0.10;

    public double computeOverallScore(InterviewSession session) {
        double score =
            (session.getInterviewScore()    * W_INTERVIEW)  +
            (session.getResumeScore() / 10.0 * W_RESUME)   +
            (session.getEmotionScore()      * W_EMOTION)    +
            (session.getCommunicationScore()* W_COMM)       +
            (session.getAttentionScore() / 10.0 * W_ATTENTION);
        return Math.round(score * 10.0) / 10.0;
    }

    public InterviewSession.Prediction predict(double overallScore, int tabSwitches, int faceMissing) {
        // Penalise cheating
        double penalty = (tabSwitches * 0.3) + (faceMissing * 0.2);
        double adjusted = Math.max(0, overallScore - penalty);

        if (adjusted >= 7.5) return InterviewSession.Prediction.SELECTED;
        if (adjusted >= 5.0) return InterviewSession.Prediction.MAYBE_SELECTED;
        return InterviewSession.Prediction.REJECTED;
    }

    public List<InterviewSession> getRankedCandidates() {
        return sessionRepository.findAllByOrderByOverallScoreDesc();
    }

    public List<InterviewSession> getRankedByRole(String role) {
        return sessionRepository.findByJobRoleOrderByOverallScoreDesc(role);
    }

    // ─── Generate learning recommendations based on weak areas ────────────────
    public void generateRecommendations(InterviewSession session, List<String> weakAreas) {
        recommendationRepository.deleteBySessionId(session.getSessionId());

        Map<String, List<String[]>> resourceMap = buildResourceMap();

        int priority = 0;
        for (String area : weakAreas) {
            String lower = area.toLowerCase();
            List<String[]> resources = null;
            for (Map.Entry<String, List<String[]>> entry : resourceMap.entrySet()) {
                if (lower.contains(entry.getKey())) {
                    resources = entry.getValue();
                    break;
                }
            }
            if (resources == null) continue;
            for (String[] res : resources) {
                LearningRecommendation rec = new LearningRecommendation();
                rec.setSessionId(session.getSessionId());
                rec.setSkill(area);
                rec.setTopic(res[0]);
                rec.setCourseTitle(res[1]);
                rec.setPlatform(res[2]);
                rec.setCourseUrl(res[3]);
                rec.setPriority(priority < 2
                    ? LearningRecommendation.Priority.HIGH
                    : priority < 4
                        ? LearningRecommendation.Priority.MEDIUM
                        : LearningRecommendation.Priority.LOW);
                recommendationRepository.save(rec);
            }
            priority++;
        }
    }

    private Map<String, List<String[]>> buildResourceMap() {
        Map<String, List<String[]>> map = new LinkedHashMap<>();
        map.put("java", Arrays.asList(
            new String[]{"Java Core", "Java Programming Masterclass", "Udemy", "https://www.udemy.com/course/java-the-complete-java-developer-course/"},
            new String[]{"Spring Boot", "Spring Boot 3 Full Course", "YouTube", "https://www.youtube.com/watch?v=9SGDpanrc8U"}
        ));
        map.put("python", Arrays.asList(
            new String[]{"Python Basics", "Python for Everybody", "Coursera", "https://www.coursera.org/specializations/python"},
            new String[]{"Python Advanced", "Python Full Course", "YouTube", "https://www.youtube.com/watch?v=_uQrJ0TkZlc"}
        ));
        map.put("sql", Arrays.asList(
            new String[]{"SQL Fundamentals", "SQL for Data Science", "Coursera", "https://www.coursera.org/learn/sql-for-data-science"},
            new String[]{"Advanced SQL", "SQL Tutorial Full", "YouTube", "https://www.youtube.com/watch?v=HXV3zeQKqGY"}
        ));
        map.put("machine learning", Arrays.asList(
            new String[]{"ML Fundamentals", "Machine Learning Specialization", "Coursera", "https://www.coursera.org/specializations/machine-learning-introduction"},
            new String[]{"Deep Learning", "Deep Learning Specialization", "Coursera", "https://www.coursera.org/specializations/deep-learning"}
        ));
        map.put("react", Arrays.asList(
            new String[]{"React Basics", "React Full Course 2024", "YouTube", "https://www.youtube.com/watch?v=bMknfKXIFA8"},
            new String[]{"React Advanced", "React - The Complete Guide", "Udemy", "https://www.udemy.com/course/react-the-complete-guide-incl-redux/"}
        ));
        map.put("docker", Arrays.asList(
            new String[]{"Docker Basics", "Docker Full Course", "YouTube", "https://www.youtube.com/watch?v=pg19Z8LL06w"},
            new String[]{"Docker & Kubernetes", "Docker and Kubernetes", "Udemy", "https://www.udemy.com/course/docker-and-kubernetes-the-complete-guide/"}
        ));
        map.put("data structure", Arrays.asList(
            new String[]{"DSA", "Data Structures and Algorithms", "Coursera", "https://www.coursera.org/specializations/data-structures-algorithms"},
            new String[]{"LeetCode Practice", "NeetCode 150", "YouTube", "https://www.youtube.com/c/NeetCode"}
        ));
        map.put("system design", Arrays.asList(
            new String[]{"System Design", "System Design Interview", "YouTube", "https://www.youtube.com/c/SystemDesignInterview"},
            new String[]{"Distributed Systems", "Grokking System Design", "Educative", "https://www.educative.io/courses/grokking-the-system-design-interview"}
        ));
        map.put("communication", Arrays.asList(
            new String[]{"Communication Skills", "Improve Public Speaking", "YouTube", "https://www.youtube.com/results?search_query=improve+technical+communication"},
            new String[]{"Presentation Skills", "Technical Communication", "Coursera", "https://www.coursera.org/learn/sciwrite"}
        ));
        return map;
    }
}
