package com.gulshan.hiresenseai.service;

import com.gulshan.hiresenseai.entity.InterviewSession;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ReportServiceTest {

    @Test
    public void testGenerateReportsWithNullScores() {
        ReportService reportService = new ReportService();
        
        InterviewSession session = new InterviewSession();
        session.setSessionId("TEST-SESSION");
        session.setJobRole("Developer");
        session.setOverallScore(null);
        session.setInterviewScore(null);
        session.setResumeScore(null);
        session.setCommunicationScore(null);
        session.setAttentionScore(null);
        session.setTabSwitchCount(null);
        session.setFinalPrediction(InterviewSession.Prediction.PENDING);
        
        assertDoesNotThrow(() -> {
            byte[] pdf = reportService.generatePdfReport(session, Collections.emptyList());
            assertNotNull(pdf);
            
            byte[] excel = reportService.generateExcelReport(List.of(session));
            assertNotNull(excel);
        });
    }
}
