package com.gulshan.hiresenseai.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
public class ResumeAnalysisService {

    private static final String[] SKILL_DB = {
        "java","python","c","c++","javascript","typescript","html","css","react","angular","vue",
        "spring","spring boot","hibernate","node","express","django","flask","fastapi",
        "sql","mysql","postgresql","mongodb","oracle","redis","elasticsearch",
        "aws","gcp","azure","docker","kubernetes","terraform","jenkins","git","github","gitlab",
        "machine learning","deep learning","artificial intelligence","data science","nlp",
        "tensorflow","pytorch","keras","opencv","pandas","numpy","scikit-learn",
        "linux","bash","shell","microservices","rest api","graphql","kafka","rabbitmq",
        "agile","scrum","jira","communication","leadership","teamwork","problem solving"
    };

    private static final Map<String, Integer> SKILL_SCORES = new HashMap<>();
    static {
        SKILL_SCORES.put("java", 15); SKILL_SCORES.put("spring", 15); SKILL_SCORES.put("spring boot", 15);
        SKILL_SCORES.put("python", 12); SKILL_SCORES.put("machine learning", 15);
        SKILL_SCORES.put("deep learning", 15); SKILL_SCORES.put("sql", 10);
        SKILL_SCORES.put("mysql", 8); SKILL_SCORES.put("docker", 12);
        SKILL_SCORES.put("kubernetes", 12); SKILL_SCORES.put("aws", 12);
        SKILL_SCORES.put("react", 10); SKILL_SCORES.put("javascript", 8);
        SKILL_SCORES.put("project", 5); SKILL_SCORES.put("internship", 8);
        SKILL_SCORES.put("git", 5); SKILL_SCORES.put("rest api", 8);
    }

    public String extractText(File pdfFile) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    public List<String> extractSkills(String resumeText) {
        List<String> skills = new ArrayList<>();
        String lower = resumeText.toLowerCase();
        for (String skill : SKILL_DB) {
            if (lower.contains(skill)) skills.add(skill);
        }
        return skills;
    }

    public int calculateResumeScore(String resumeText) {
        String lower = resumeText.toLowerCase();
        int score = 0;
        for (Map.Entry<String, Integer> entry : SKILL_SCORES.entrySet()) {
            if (lower.contains(entry.getKey())) score += entry.getValue();
        }
        return Math.min(100, score);
    }

    public List<String> getMissingSkills(String resumeText, String jobRole) {
        String lower = resumeText.toLowerCase();
        List<String> missing = new ArrayList<>();
        Map<String, List<String>> roleRequired = Map.of(
            "java", Arrays.asList("spring boot", "sql", "git", "docker", "rest api"),
            "ai", Arrays.asList("python", "machine learning", "tensorflow", "pandas", "numpy"),
            "web", Arrays.asList("javascript", "react", "html", "css", "rest api"),
            "data", Arrays.asList("python", "sql", "pandas", "machine learning", "tableau"),
            "devops", Arrays.asList("docker", "kubernetes", "jenkins", "aws", "terraform"),
            "cloud", Arrays.asList("aws", "docker", "kubernetes", "terraform", "linux")
        );
        String key = jobRole != null ? jobRole.toLowerCase() : "";
        List<String> required = null;
        for (Map.Entry<String, List<String>> e : roleRequired.entrySet()) {
            if (key.contains(e.getKey())) { required = e.getValue(); break; }
        }
        if (required != null) {
            for (String skill : required) {
                if (!lower.contains(skill)) missing.add(skill);
            }
        }
        return missing;
    }

    public List<String> generateQuestionsFromResume(String resumeText, String jobRole, int count) {
        // Uses the extracted skills to call QuestionGeneratorService externally
        return Collections.emptyList(); // delegated to QuestionGeneratorService
    }

    public File saveUploadedFile(MultipartFile file, String uploadDir) throws Exception {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String safeName = Objects.requireNonNull(file.getOriginalFilename()).replaceAll("[^a-zA-Z0-9._-]", "_");
        File dest = new File(dir, safeName);
        file.transferTo(dest.toPath());
        return dest;
    }
}
