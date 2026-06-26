package com.gulshan.hiresenseai.service;

import com.gulshan.hiresenseai.entity.InterviewAnswer;
import com.gulshan.hiresenseai.entity.InterviewSession;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    // ─── PDF Report ────────────────────────────────────────────────────────────
    public byte[] generatePdfReport(InterviewSession session, List<InterviewAnswer> answers) throws Exception {
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Fonts
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 22, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        com.itextpdf.text.Font headingFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 13, com.itextpdf.text.Font.BOLD, new BaseColor(50, 50, 120));
        com.itextpdf.text.Font bodyFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
        com.itextpdf.text.Font labelFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new BaseColor(80, 80, 80));

        // Header banner
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell hCell = new PdfPCell(new Phrase("HireSense AI — Interview Report", titleFont));
        hCell.setBackgroundColor(new BaseColor(30, 30, 80));
        hCell.setPadding(18);
        hCell.setBorder(Rectangle.NO_BORDER);
        hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(hCell);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        // Session Info
        doc.add(new Paragraph("Session Overview", headingFont));
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{1, 2});
        addInfoRow(info, "Session ID", session.getSessionId(), labelFont, bodyFont);
        addInfoRow(info, "Job Role", session.getJobRole(), labelFont, bodyFont);
        if (session.getStartTime() != null)
            addInfoRow(info, "Date", session.getStartTime().format(FMT), labelFont, bodyFont);
        addInfoRow(info, "Final Result", session.getFinalPrediction().name().replace("_", " "), labelFont, bodyFont);
        doc.add(info);
        doc.add(Chunk.NEWLINE);

        // Score Summary Table
        doc.add(new Paragraph("Performance Summary", headingFont));
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        PdfPTable scores = new PdfPTable(2);
        scores.setWidthPercentage(60);
        addScoreRow(scores, "Overall Score", String.format("%.1f / 10", orZero(session.getOverallScore())), labelFont, bodyFont);
        addScoreRow(scores, "Interview Score", String.format("%.1f / 10", orZero(session.getInterviewScore())), labelFont, bodyFont);
        addScoreRow(scores, "Resume Score", String.format("%.0f / 100", orZero(session.getResumeScore())), labelFont, bodyFont);
        addScoreRow(scores, "Communication Score", String.format("%.1f / 10", orZero(session.getCommunicationScore())), labelFont, bodyFont);
        addScoreRow(scores, "Attention Score", String.format("%.0f%%", orZero(session.getAttentionScore())), labelFont, bodyFont);
        addScoreRow(scores, "Tab Switches (Cheating)", String.valueOf(orZero(session.getTabSwitchCount())), labelFont, bodyFont);
        doc.add(scores);
        doc.add(Chunk.NEWLINE);

        // Question Answers
        doc.add(new Paragraph("Interview Q&A Breakdown", headingFont));
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        int qNum = 1;
        for (InterviewAnswer ans : answers) {
            PdfPTable qTable = new PdfPTable(1);
            qTable.setWidthPercentage(100);
            qTable.setSpacingAfter(10);

            // Question header
            PdfPCell qCell = new PdfPCell(new Phrase("Q" + qNum + ": " + ans.getQuestion(), labelFont));
            qCell.setBackgroundColor(new BaseColor(240, 240, 255));
            qCell.setPadding(8);
            qTable.addCell(qCell);

            // Answer
            PdfPCell aCell = new PdfPCell(new Phrase("Answer: " + (ans.getAnswer() != null ? ans.getAnswer() : "Not provided"), bodyFont));
            aCell.setPadding(8);
            qTable.addCell(aCell);

            // Scores line
            String scoreStr = String.format("Scores — Relevance: %.1f  Technical: %.1f  Completeness: %.1f  Grammar: %.1f",
                orZero(ans.getRelevanceScore()), orZero(ans.getTechnicalScore()),
                orZero(ans.getCompletenessScore()), orZero(ans.getGrammarScore()));
            PdfPCell sCell = new PdfPCell(new Phrase(scoreStr, bodyFont));
            sCell.setPadding(6);
            sCell.setBackgroundColor(new BaseColor(250, 250, 250));
            qTable.addCell(sCell);

            // AI Feedback
            String fb = ans.getAiFeedback() != null ? ans.getAiFeedback() : ans.getFeedback();
            PdfPCell fbCell = new PdfPCell(new Phrase("Feedback: " + (fb != null ? fb : "N/A"), bodyFont));
            fbCell.setPadding(6);
            qTable.addCell(fbCell);

            doc.add(qTable);
            qNum++;
        }

        // Footer
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("Generated by HireSense AI | www.hiresenseai.com", bodyFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    // ─── Excel Export (all candidates) ────────────────────────────────────────
    public byte[] generateExcelReport(List<InterviewSession> sessions) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Candidates");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font hFont = wb.createFont();
            hFont.setColor(IndexedColors.WHITE.getIndex());
            hFont.setBold(true);
            headerStyle.setFont(hFont);

            String[] cols = {"Session ID","Candidate","Email","Job Role","Overall","Interview",
                             "Resume","Communication","Attention","Tab Switches","Prediction","Date"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Data rows
            int rowIdx = 1;
            for (InterviewSession s : sessions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getSessionId());
                row.createCell(1).setCellValue(s.getUser() != null ? s.getUser().getName() : "N/A");
                row.createCell(2).setCellValue(s.getUser() != null ? s.getUser().getEmail() : "N/A");
                row.createCell(3).setCellValue(s.getJobRole());
                row.createCell(4).setCellValue(orZero(s.getOverallScore()));
                row.createCell(5).setCellValue(orZero(s.getInterviewScore()));
                row.createCell(6).setCellValue(orZero(s.getResumeScore()));
                row.createCell(7).setCellValue(orZero(s.getCommunicationScore()));
                row.createCell(8).setCellValue(orZero(s.getAttentionScore()));
                row.createCell(9).setCellValue(orZero(s.getTabSwitchCount()));
                row.createCell(10).setCellValue(s.getFinalPrediction().name());
                row.createCell(11).setCellValue(s.getStartTime() != null ? s.getStartTime().format(FMT) : "N/A");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────
    private void addInfoRow(PdfPTable t, String label, String value,
                            com.itextpdf.text.Font lf, com.itextpdf.text.Font bf) {
        PdfPCell l = new PdfPCell(new Phrase(label, lf)); l.setPadding(5); l.setBorder(Rectangle.NO_BORDER);
        PdfPCell v = new PdfPCell(new Phrase(value != null ? value : "N/A", bf)); v.setPadding(5); v.setBorder(Rectangle.NO_BORDER);
        t.addCell(l); t.addCell(v);
    }

    private void addScoreRow(PdfPTable t, String label, String value,
                             com.itextpdf.text.Font lf, com.itextpdf.text.Font bf) {
        PdfPCell l = new PdfPCell(new Phrase(label, lf)); l.setPadding(6);
        PdfPCell v = new PdfPCell(new Phrase(value, bf)); v.setPadding(6);
        t.addCell(l); t.addCell(v);
    }

    private double orZero(Double v) { return v != null ? v : 0.0; }
    private int orZero(Integer v) { return v != null ? v : 0; }
}
