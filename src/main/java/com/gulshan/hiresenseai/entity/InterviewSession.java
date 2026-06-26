package com.gulshan.hiresenseai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String jobRole;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // ─── Scores ────────────────────────────────────────────────────────────────
    private Double resumeScore = 0.0;
    private Double interviewScore = 0.0;
    private Double emotionScore = 0.0;
    private Double communicationScore = 0.0;
    private Double attentionScore = 100.0;
    private Double overallScore = 0.0;

    // ─── Anti-Cheat ────────────────────────────────────────────────────────────
    private Integer tabSwitchCount = 0;
    private Integer faceMissingCount = 0;

    // ─── Final Result ──────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    private Prediction finalPrediction = Prediction.PENDING;

    private String videoRecordingPath;

    public enum Prediction {
        PENDING, SELECTED, MAYBE_SELECTED, REJECTED
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getResumeScore() { return resumeScore; }
    public void setResumeScore(Double resumeScore) { this.resumeScore = resumeScore; }

    public Double getInterviewScore() { return interviewScore; }
    public void setInterviewScore(Double interviewScore) { this.interviewScore = interviewScore; }

    public Double getEmotionScore() { return emotionScore; }
    public void setEmotionScore(Double emotionScore) { this.emotionScore = emotionScore; }

    public Double getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(Double communicationScore) { this.communicationScore = communicationScore; }

    public Double getAttentionScore() { return attentionScore; }
    public void setAttentionScore(Double attentionScore) { this.attentionScore = attentionScore; }

    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }

    public Integer getTabSwitchCount() { return tabSwitchCount; }
    public void setTabSwitchCount(Integer tabSwitchCount) { this.tabSwitchCount = tabSwitchCount; }

    public Integer getFaceMissingCount() { return faceMissingCount; }
    public void setFaceMissingCount(Integer faceMissingCount) { this.faceMissingCount = faceMissingCount; }

    public Prediction getFinalPrediction() { return finalPrediction; }
    public void setFinalPrediction(Prediction finalPrediction) { this.finalPrediction = finalPrediction; }

    public String getVideoRecordingPath() { return videoRecordingPath; }
    public void setVideoRecordingPath(String videoRecordingPath) { this.videoRecordingPath = videoRecordingPath; }
}
