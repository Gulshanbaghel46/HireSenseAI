package com.gulshan.hiresenseai.controller;

import com.gulshan.hiresenseai.entity.*;
import com.gulshan.hiresenseai.repository.*;
import com.gulshan.hiresenseai.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/users")
public class InterviewController {

    @Autowired private UserRepository userRepository;
    @Autowired private InterviewSessionRepository sessionRepository;
    @Autowired private InterviewAnswerRepository answerRepository;
    @Autowired private CheatLogRepository cheatLogRepository;
    @Autowired private EmotionLogRepository emotionLogRepository;
    @Autowired private LearningRecommendationRepository recommendationRepository;
    @Autowired private AIEvaluationService aiEvaluationService;
    @Autowired private QuestionGeneratorService questionGeneratorService;
    @Autowired private ResumeAnalysisService resumeAnalysisService;
    @Autowired private ReportService reportService;
    @Autowired private RankingService rankingService;
    @Autowired private EmailService emailService;

    @Value("${hiresense.uploads.dir:${user.home}/hiresenseai/uploads}")
    private String uploadsDir;

    private boolean isLoggedIn(HttpSession s) { return s.getAttribute("userId") != null; }

    // ─── Resume Upload & Analysis ──────────────────────────────────────────────
    @GetMapping("/resume")
    public String resumePage(@RequestParam(required = false) String jobRole,
                             HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";
        model.addAttribute("jobRole", jobRole);
        model.addAttribute("roles", questionGeneratorService.getAvailableRoles());
        return "resume";
    }

    @PostMapping("/uploadResume")
    public String uploadResume(@RequestParam("file") MultipartFile file,
                               @RequestParam(required = false) String jobRole,
                               HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";
        try {
            if (file.isEmpty()) { model.addAttribute("message", "Please select a PDF file."); return "resume"; }
            java.io.File saved = resumeAnalysisService.saveUploadedFile(file, uploadsDir);
            String resumeText = resumeAnalysisService.extractText(saved);
            List<String> skills = resumeAnalysisService.extractSkills(resumeText);
            int score = resumeAnalysisService.calculateResumeScore(resumeText);
            List<String> missing = resumeAnalysisService.getMissingSkills(resumeText, jobRole);
            List<String> questions = questionGeneratorService.generateQuestions(jobRole, skills, 10);

            // Persist resume score on session
            session.setAttribute("resumeScore", (double) score);
            session.setAttribute("pendingJobRole", jobRole);
            session.setAttribute("pendingQuestions", questions);

            model.addAttribute("resumeText", resumeText);
            model.addAttribute("skills", skills);
            model.addAttribute("resumeScore", score);
            model.addAttribute("missingSkills", missing);
            model.addAttribute("questions", questions);
            model.addAttribute("jobRole", jobRole);
            return "analysis";
        } catch (Exception e) {
            model.addAttribute("message", "Error processing PDF: " + e.getMessage());
            return "resume";
        }
    }

    // ─── Interview Session ─────────────────────────────────────────────────────
    @GetMapping("/interview")
    public String interviewPage(@RequestParam(defaultValue = "0") int index,
                                @RequestParam(required = false) String sessionId,
                                @RequestParam(required = false) String jobRole,
                                HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";

        Long userId = (Long) session.getAttribute("userId");
        String role = jobRole != null ? jobRole : (String) session.getAttribute("pendingJobRole");
        if (role == null) role = "Java Developer";

        // Create or fetch session
        InterviewSession interviewSession;
        if (sessionId == null) {
            sessionId = String.valueOf(System.currentTimeMillis());
            interviewSession = new InterviewSession();
            interviewSession.setSessionId(sessionId);
            interviewSession.setUser(userRepository.findById(userId).orElse(null));
            interviewSession.setJobRole(role);
            interviewSession.setStartTime(LocalDateTime.now());
            Double rs = (Double) session.getAttribute("resumeScore");
            interviewSession.setResumeScore(rs != null ? rs : 0.0);
            sessionRepository.save(interviewSession);
        } else {
            interviewSession = sessionRepository.findBySessionId(sessionId).orElse(null);
        }

        // Load or generate questions
        @SuppressWarnings("unchecked")
        List<String> questions = (List<String>) session.getAttribute("pendingQuestions");
        if (questions == null || questions.isEmpty()) {
            questions = questionGeneratorService.generateQuestions(role, null, 5);
        }

        if (index >= questions.size()) {
            return "redirect:/users/report?sessionId=" + sessionId;
        }

        model.addAttribute("question", questions.get(index));
        model.addAttribute("index", index);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("jobRole", role);
        model.addAttribute("questions", questions);
        return "interview";
    }

    @PostMapping("/submitAnswer")
    public String submitAnswer(@RequestParam String question, @RequestParam String answer,
                               @RequestParam int index, @RequestParam String sessionId,
                               @RequestParam(required = false) String jobRole,
                               HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/users/login";
        if (jobRole == null) jobRole = (String) session.getAttribute("pendingJobRole");
        if (jobRole == null) jobRole = "Java Developer";

        // AI Evaluation
        Map<String, Object> eval = aiEvaluationService.evaluateAnswer(question, answer, jobRole);

        InterviewAnswer ia = new InterviewAnswer();
        ia.setSessionId(sessionId);
        ia.setQuestion(question);
        ia.setAnswer(answer);
        ia.setRelevanceScore((Double) eval.get("relevance"));
        ia.setTechnicalScore((Double) eval.get("technical"));
        ia.setConfidenceScore((Double) eval.get("confidence"));
        ia.setCompletenessScore((Double) eval.get("completeness"));
        ia.setGrammarScore((Double) eval.get("grammar"));
        ia.setVocabularyScore((Double) eval.get("vocabulary"));
        ia.setFluencyScore((Double) eval.get("fluency"));
        ia.setAiFeedback((String) eval.get("feedback"));
        // Legacy score = average of technical + relevance
        double legacy = ((Double) eval.get("relevance") + (Double) eval.get("technical")) / 2.0;
        ia.setScore((int) Math.round(legacy));
        ia.setFeedback((String) eval.get("feedback"));
        answerRepository.save(ia);

        return "redirect:/users/interview?index=" + (index + 1) + "&sessionId=" + sessionId + "&jobRole=" + jobRole;
    }

    // ─── Cheat Log (REST endpoint called from JS) ──────────────────────────────
    @PostMapping("/logCheat")
    @ResponseBody
    public ResponseEntity<String> logCheat(@RequestParam String sessionId,
                                           @RequestParam String type,
                                           HttpSession session) {
        if (!isLoggedIn(session)) return ResponseEntity.status(401).body("Unauthorized");
        CheatLog log = new CheatLog();
        log.setSessionId(sessionId);
        log.setTimestamp(LocalDateTime.now());
        log.setDescription("Detected: " + type);
        try {
            log.setType(CheatLog.CheatType.valueOf(type));
        } catch (Exception e) {
            log.setType(CheatLog.CheatType.TAB_SWITCH);
        }
        cheatLogRepository.save(log);

        // Increment counter on session
        sessionRepository.findBySessionId(sessionId).ifPresent(s -> {
            if ("TAB_SWITCH".equals(type)) s.setTabSwitchCount((s.getTabSwitchCount() != null ? s.getTabSwitchCount() : 0) + 1);
            else if ("FACE_MISSING".equals(type)) s.setFaceMissingCount((s.getFaceMissingCount() != null ? s.getFaceMissingCount() : 0) + 1);
            sessionRepository.save(s);
        });
        return ResponseEntity.ok("logged");
    }

    // ─── Emotion Log (REST endpoint called from JS) ────────────────────────────
    @PostMapping("/logEmotion")
    @ResponseBody
    public ResponseEntity<String> logEmotion(@RequestParam String sessionId,
                                             @RequestParam String emotion,
                                             @RequestParam(defaultValue = "true") boolean eyeContact,
                                             @RequestParam(defaultValue = "true") boolean facePresent,
                                             HttpSession session) {
        if (!isLoggedIn(session)) return ResponseEntity.status(401).body("Unauthorized");
        EmotionLog log = new EmotionLog();
        log.setSessionId(sessionId);
        log.setTimestamp(LocalDateTime.now());
        log.setEmotion(emotion);
        log.setEyeContact(eyeContact);
        log.setFacePresent(facePresent);
        emotionLogRepository.save(log);
        return ResponseEntity.ok("logged");
    }

    // ─── Report Page ───────────────────────────────────────────────────────────
    @GetMapping("/report")
    public String reportPage(@RequestParam String sessionId, HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";

        InterviewSession interviewSession = sessionRepository.findBySessionId(sessionId).orElse(null);
        List<InterviewAnswer> answers = answerRepository.findBySessionId(sessionId);

        // Calculate aggregated scores
        if (interviewSession != null && !answers.isEmpty()) {
            double tech = answers.stream().mapToDouble(a -> orZero(a.getTechnicalScore())).average().orElse(0);
            double rel  = answers.stream().mapToDouble(a -> orZero(a.getRelevanceScore())).average().orElse(0);
            double comm = answers.stream().mapToDouble(a -> orZero(a.getGrammarScore()) + orZero(a.getFluencyScore())).average().orElse(0) / 2.0;
            double conf = answers.stream().mapToDouble(a -> orZero(a.getConfidenceScore())).average().orElse(0);

            interviewSession.setInterviewScore(Math.round(((tech + rel) / 2.0) * 10.0) / 10.0);
            interviewSession.setCommunicationScore(Math.round(comm * 10.0) / 10.0);
            interviewSession.setEmotionScore(conf);

            int tabSwitches = interviewSession.getTabSwitchCount() != null ? interviewSession.getTabSwitchCount() : 0;
            int faceMissing = interviewSession.getFaceMissingCount() != null ? interviewSession.getFaceMissingCount() : 0;
            double attScore = Math.max(0.0, 100.0 - (tabSwitches * 10.0) - (faceMissing * 5.0));
            interviewSession.setAttentionScore(attScore);

            double overall = rankingService.computeOverallScore(interviewSession);
            interviewSession.setOverallScore(overall);
            interviewSession.setFinalPrediction(rankingService.predict(overall, tabSwitches, faceMissing));
            interviewSession.setEndTime(LocalDateTime.now());
            sessionRepository.save(interviewSession);

            // Generate learning recommendations for weak areas
            List<String> weakAreas = new ArrayList<>();
            if (tech < 5) weakAreas.add("technical skills for " + interviewSession.getJobRole());
            if (comm < 5) weakAreas.add("communication");
            if (rel < 5) weakAreas.add("answer relevance and depth");
            if (!weakAreas.isEmpty()) rankingService.generateRecommendations(interviewSession, weakAreas);

            // Send email report async
            Long userId = (Long) session.getAttribute("userId");
            userRepository.findById(userId).ifPresent(u -> {
                try {
                    byte[] pdf = reportService.generatePdfReport(interviewSession, answers);
                    emailService.sendInterviewReport(u.getEmail(), u.getName(), interviewSession, pdf);
                } catch (Exception e) {
                    System.err.println("PDF/Email error: " + e.getMessage());
                }
            });
        }

        // Safety guard: ensure finalPrediction is never null to prevent Thymeleaf NPE
        if (interviewSession != null && interviewSession.getFinalPrediction() == null) {
            interviewSession.setFinalPrediction(InterviewSession.Prediction.REJECTED);
            interviewSession.setOverallScore(interviewSession.getOverallScore() != null ? interviewSession.getOverallScore() : 0.0);
            sessionRepository.save(interviewSession);
        }

        // Emotion summary
        List<Object[]> emotionCounts = emotionLogRepository.countEmotionsBySession(sessionId);
        List<CheatLog> cheatLogs = cheatLogRepository.findBySessionIdOrderByTimestamp(sessionId);
        List<LearningRecommendation> recommendations = recommendationRepository.findBySessionIdOrderByPriority(sessionId);

        model.addAttribute("interviewSession", interviewSession);
        model.addAttribute("answers", answers != null ? answers : new java.util.ArrayList<>());
        model.addAttribute("emotionCounts", emotionCounts);
        model.addAttribute("cheatLogs", cheatLogs);
        model.addAttribute("recommendations", recommendations != null ? recommendations : new java.util.ArrayList<>());
        model.addAttribute("sessionId", sessionId);
        return "report";
    }

    // ─── History ───────────────────────────────────────────────────────────────
    @GetMapping("/history")
    public String historyPage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";
        Long userId = (Long) session.getAttribute("userId");
        List<InterviewSession> sessions = sessionRepository.findByUserId(userId);
        model.addAttribute("sessions", sessions);
        return "history";
    }

    // ─── Download PDF ──────────────────────────────────────────────────────────
    @GetMapping("/downloadReport")
    public ResponseEntity<byte[]> downloadReport(@RequestParam String sessionId, HttpSession session) throws Exception {
        if (!isLoggedIn(session)) return ResponseEntity.status(302).header("Location", "/users/login").build();
        InterviewSession interviewSession = sessionRepository.findBySessionId(sessionId).orElse(null);
        List<InterviewAnswer> answers = answerRepository.findBySessionId(sessionId);
        if (interviewSession == null) return ResponseEntity.notFound().build();
        byte[] pdf = reportService.generatePdfReport(interviewSession, answers);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=report_" + sessionId + ".pdf")
            .header("Content-Type", "application/pdf")
            .body(pdf);
    }

    // ─── Download Excel (own sessions) ─────────────────────────────────────────
    @GetMapping("/downloadExcel")
    public ResponseEntity<byte[]> downloadExcel(HttpSession session) throws Exception {
        if (!isLoggedIn(session)) return ResponseEntity.status(302).header("Location", "/users/login").build();
        Long userId = (Long) session.getAttribute("userId");
        List<InterviewSession> sessions = sessionRepository.findByUserId(userId);
        byte[] excel = reportService.generateExcelReport(sessions);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=my_interviews.xlsx")
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .body(excel);
    }

    // ─── Analytics REST (Chart.js data) ───────────────────────────────────────
    @GetMapping("/analyticsData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyticsData(@RequestParam String sessionId, HttpSession session) {
        if (!isLoggedIn(session)) return ResponseEntity.status(401).build();
        List<Object[]> emotions = emotionLogRepository.countEmotionsBySession(sessionId);
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Object[] row : emotions) { labels.add((String) row[0]); counts.add((Long) row[1]); }
        data.put("emotionLabels", labels);
        data.put("emotionCounts", counts);
        return ResponseEntity.ok(data);
    }

    private double orZero(Double v) { return v != null ? v : 0.0; }
}
