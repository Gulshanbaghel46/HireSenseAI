package com.gulshan.hiresenseai.controller;

import com.gulshan.hiresenseai.entity.InterviewSession;
import com.gulshan.hiresenseai.repository.InterviewSessionRepository;
import com.gulshan.hiresenseai.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private ReportService reportService;

    private boolean isRecruiter(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        return "RECRUITER".equals(role) || "ADMIN".equals(role);
    }

    @GetMapping("/dashboard")
    public String recruiterDashboard(HttpSession session, Model model) {
        if (!isRecruiter(session)) {
            return "redirect:/users/login";
        }
        
        List<InterviewSession> sessions = sessionRepository.findAllByOrderByOverallScoreDesc();
        model.addAttribute("sessions", sessions);
        model.addAttribute("name", session.getAttribute("userName"));
        return "recruiter";
    }

    @GetMapping("/downloadExcelReport")
    public ResponseEntity<byte[]> downloadExcelReport(HttpSession session) throws Exception {
        if (!isRecruiter(session)) {
            return ResponseEntity.status(302).header("Location", "/users/login").build();
        }
        List<InterviewSession> sessions = sessionRepository.findAllByOrderByOverallScoreDesc();
        byte[] excel = reportService.generateExcelReport(sessions);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=all_candidates_report.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }
}
