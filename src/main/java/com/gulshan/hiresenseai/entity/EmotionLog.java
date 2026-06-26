package com.gulshan.hiresenseai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_logs")
public class EmotionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private LocalDateTime timestamp;
    private String emotion;      // happy, sad, angry, fear, disgust, surprise, neutral
    private Double confidence;   // confidence of the emotion detection
    private Boolean eyeContact;  // true if looking at screen
    private Boolean facePresent; // true if face detected

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Boolean getEyeContact() { return eyeContact; }
    public void setEyeContact(Boolean eyeContact) { this.eyeContact = eyeContact; }

    public Boolean getFacePresent() { return facePresent; }
    public void setFacePresent(Boolean facePresent) { this.facePresent = facePresent; }
}
