package com.gulshan.hiresenseai.service;

import com.gulshan.hiresenseai.entity.InterviewSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${hiresense.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:noreply@hiresenseai.com}")
    private String fromEmail;

    @Async
    public void sendInterviewReport(String toEmail, String candidateName,
                                    InterviewSession session, byte[] pdfBytes) {
        if (!mailEnabled || mailSender == null) {
            System.out.println("[Email] Skipped (mail disabled): Report for " + toEmail);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your HireSense AI Interview Report — " + session.getJobRole());

            String prediction = session.getFinalPrediction().name().replace("_", " ");
            String html = buildEmailHtml(candidateName, session, prediction);
            helper.setText(html, true);

            if (pdfBytes != null && pdfBytes.length > 0) {
                helper.addAttachment("InterviewReport_" + session.getSessionId() + ".pdf",
                    () -> new java.io.ByteArrayInputStream(pdfBytes), "application/pdf");
            }
            mailSender.send(msg);
            System.out.println("[Email] Report sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("[Email] Failed to send: " + e.getMessage());
        }
    }

    private String buildEmailHtml(String name, InterviewSession session, String prediction) {
        String color = switch (session.getFinalPrediction()) {
            case SELECTED -> "#10b981";
            case MAYBE_SELECTED -> "#f59e0b";
            case REJECTED -> "#ef4444";
            default -> "#6366f1";
        };
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f9fafb;padding:30px;border-radius:12px;">
              <div style="background:linear-gradient(135deg,#1e1b4b,#312e81);padding:30px;border-radius:8px;text-align:center;margin-bottom:24px;">
                <h1 style="color:white;margin:0;font-size:24px;">HireSense AI</h1>
                <p style="color:#a5b4fc;margin:8px 0 0;">Interview Completed</p>
              </div>
              <p style="font-size:16px;">Hi <strong>%s</strong>,</p>
              <p>Your AI mock interview for <strong>%s</strong> has been completed. Here is your performance summary:</p>
              <table style="width:100%%;border-collapse:collapse;margin:20px 0;">
                <tr><td style="padding:10px;background:#f3f4f6;font-weight:bold;">Overall Score</td><td style="padding:10px;">%.1f / 10</td></tr>
                <tr><td style="padding:10px;font-weight:bold;">Interview Score</td><td style="padding:10px;">%.1f / 10</td></tr>
                <tr><td style="padding:10px;background:#f3f4f6;font-weight:bold;">Communication Score</td><td style="padding:10px;">%.1f / 10</td></tr>
                <tr><td style="padding:10px;font-weight:bold;">Final Prediction</td>
                    <td style="padding:10px;"><span style="background:%s;color:white;padding:4px 12px;border-radius:20px;font-size:14px;">%s</span></td></tr>
              </table>
              <p>Your detailed report is attached as a PDF. Log in to your HireSense AI dashboard for full analytics and personalized learning recommendations.</p>
              <div style="text-align:center;margin-top:30px;">
                <a href="http://localhost:8081/users/dashboard" style="background:#6366f1;color:white;padding:12px 30px;border-radius:8px;text-decoration:none;font-weight:bold;">View Dashboard</a>
              </div>
              <p style="color:#9ca3af;font-size:12px;text-align:center;margin-top:30px;">HireSense AI • AI-Powered Interview Platform</p>
            </div>
            """.formatted(name, session.getJobRole(),
                session.getOverallScore(), session.getInterviewScore(),
                session.getCommunicationScore(), color, prediction);
    }
}
