package com.gulshan.hiresenseai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cheat_logs")
public class CheatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private CheatType type;

    private String description;

    public enum CheatType {
        TAB_SWITCH, FACE_MISSING, MULTIPLE_FACES, LOOKING_AWAY
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public CheatType getType() { return type; }
    public void setType(CheatType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
