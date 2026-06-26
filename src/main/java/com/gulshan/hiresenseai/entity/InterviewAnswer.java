package com.gulshan.hiresenseai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_answers")
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    @Column(length = 2000)
    private String question;

    @Column(length = 8000)
    private String answer;

    // ─── Legacy simple score ───────────────────────────────────────────────────
    private int score;

    // ─── AI Evaluation Scores (1-10 each) ────────────────────────────────────
    private Double relevanceScore = 0.0;
    private Double technicalScore = 0.0;
    private Double confidenceScore = 0.0;
    private Double completenessScore = 0.0;

    // ─── Communication scores ─────────────────────────────────────────────────
    private Double grammarScore = 0.0;
    private Double vocabularyScore = 0.0;
    private Double fluencyScore = 0.0;

    @Column(length = 3000)
    private String feedback;

    @Column(length = 3000)
    private String aiFeedback;

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }

    public Double getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public Double getCompletenessScore() { return completenessScore; }
    public void setCompletenessScore(Double completenessScore) { this.completenessScore = completenessScore; }

    public Double getGrammarScore() { return grammarScore; }
    public void setGrammarScore(Double grammarScore) { this.grammarScore = grammarScore; }

    public Double getVocabularyScore() { return vocabularyScore; }
    public void setVocabularyScore(Double vocabularyScore) { this.vocabularyScore = vocabularyScore; }

    public Double getFluencyScore() { return fluencyScore; }
    public void setFluencyScore(Double fluencyScore) { this.fluencyScore = fluencyScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
}