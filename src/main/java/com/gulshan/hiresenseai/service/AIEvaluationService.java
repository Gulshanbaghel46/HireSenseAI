package com.gulshan.hiresenseai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AIEvaluationService — calls the Groq LLM API to evaluate interview answers
 * and generate intelligent feedback. Falls back gracefully if the API key is
 * not configured.
 */
@Service
public class AIEvaluationService {

    @Value("${groq.api.key:your-groq-api-key-here}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama3-8b-8192}")
    private String model;

    @Value("${hiresense.ai.evaluation.enabled:true}")
    private boolean aiEnabled;

    private final ObjectMapper mapper = new ObjectMapper();

    // ─── Main evaluation method ────────────────────────────────────────────────
    /**
     * Evaluates a candidate's answer and returns scores for:
     * relevance, technical, confidence, completeness, grammar, vocabulary, fluency
     * plus detailed AI feedback.
     */
    public Map<String, Object> evaluateAnswer(String question, String answer, String jobRole) {
        if (!aiEnabled || apiKey.equals("your-groq-api-key-here") || apiKey.isBlank()) {
            return fallbackEvaluation(answer);
        }
        try {
            String prompt = buildEvaluationPrompt(question, answer, jobRole);
            String response = callGroqAPI(prompt);
            return parseEvaluationResponse(response);
        } catch (Exception e) {
            System.err.println("Groq API error: " + e.getMessage());
            return fallbackEvaluation(answer);
        }
    }

    // ─── Question Generation ───────────────────────────────────────────────────
    public String generateFollowUpQuestion(String previousQuestion, String previousAnswer, String jobRole) {
        if (!aiEnabled || apiKey.equals("your-groq-api-key-here") || apiKey.isBlank()) {
            return null;
        }
        try {
            String prompt = "You are an expert " + jobRole + " interviewer. " +
                "The candidate answered: \"" + previousAnswer + "\" to the question: \"" + previousQuestion + "\". " +
                "Generate ONE concise follow-up interview question (no preamble, just the question):";
            String raw = callGroqAPI(prompt);
            return extractTextContent(raw);
        } catch (Exception e) {
            return null;
        }
    }

    public String generateLearningRecommendations(String weakAreas, String jobRole) {
        if (!aiEnabled || apiKey.equals("your-groq-api-key-here") || apiKey.isBlank()) {
            return null;
        }
        try {
            String prompt = "Based on these weak areas in a " + jobRole + " interview: " + weakAreas +
                ", recommend 3-5 specific topics to study and free online resources. Format as JSON array: " +
                "[{\"topic\": \"Topic Name\", \"platform\": \"YouTube\", \"course\": \"Course Title\", " +
                "\"url\": \"https://...\", \"priority\": \"HIGH\"}]";
            String raw = callGroqAPI(prompt);
            return extractTextContent(raw);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Internal helpers ──────────────────────────────────────────────────────
    private String buildEvaluationPrompt(String question, String answer, String jobRole) {
        return "You are an expert technical interviewer evaluating a " + jobRole + " candidate.\n" +
            "Question: \"" + question + "\"\n" +
            "Candidate Answer: \"" + answer + "\"\n\n" +
            "Rate this answer on a scale of 1-10 for each dimension and provide feedback. " +
            "Respond ONLY in this exact JSON format:\n" +
            "{\n" +
            "  \"relevance\": 7,\n" +
            "  \"technical\": 6,\n" +
            "  \"confidence\": 7,\n" +
            "  \"completeness\": 6,\n" +
            "  \"grammar\": 8,\n" +
            "  \"vocabulary\": 7,\n" +
            "  \"fluency\": 7,\n" +
            "  \"feedback\": \"Your feedback here in 2-3 sentences.\"\n" +
            "}";
    }

    private String callGroqAPI(String userMessage) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(apiUrl);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", 0.3);
            body.put("max_tokens", 512);

            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", userMessage);

            post.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            return client.execute(post, response -> {
                byte[] bytes = response.getEntity().getContent().readAllBytes();
                return new String(bytes);
            });
        }
    }

    private Map<String, Object> parseEvaluationResponse(String rawResponse) {
        try {
            JsonNode root = mapper.readTree(rawResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            // Extract JSON from content
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                String jsonStr = content.substring(start, end + 1);
                JsonNode scores = mapper.readTree(jsonStr);

                Map<String, Object> result = new HashMap<>();
                result.put("relevance", scores.path("relevance").asDouble(5.0));
                result.put("technical", scores.path("technical").asDouble(5.0));
                result.put("confidence", scores.path("confidence").asDouble(5.0));
                result.put("completeness", scores.path("completeness").asDouble(5.0));
                result.put("grammar", scores.path("grammar").asDouble(5.0));
                result.put("vocabulary", scores.path("vocabulary").asDouble(5.0));
                result.put("fluency", scores.path("fluency").asDouble(5.0));
                result.put("feedback", scores.path("feedback").asText("Good attempt."));
                return result;
            }
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
        }
        return fallbackEvaluation("");
    }

    private String extractTextContent(String rawResponse) {
        try {
            JsonNode root = mapper.readTree(rawResponse);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fallback scoring when AI is disabled or unavailable.
     * Uses heuristic keyword-based evaluation.
     */
    private Map<String, Object> fallbackEvaluation(String answer) {
        Map<String, Object> result = new HashMap<>();
        if (answer == null || answer.isBlank()) {
            result.put("relevance", 1.0); result.put("technical", 1.0);
            result.put("confidence", 1.0); result.put("completeness", 1.0);
            result.put("grammar", 1.0); result.put("vocabulary", 1.0);
            result.put("fluency", 1.0);
            result.put("feedback", "No answer provided.");
            return result;
        }
        int wordCount = answer.trim().split("\\s+").length;
        double baseScore = Math.min(10.0, 3.0 + (wordCount / 15.0));
        // Boost for technical keywords
        String lower = answer.toLowerCase();
        double techBoost = 0;
        for (String kw : new String[]{"java","python","api","class","object","inheritance",
                "database","sql","spring","algorithm","function","method","interface"}) {
            if (lower.contains(kw)) techBoost += 0.3;
        }
        double tech = Math.min(10.0, baseScore + techBoost);
        double comm = Math.min(10.0, baseScore + (wordCount > 50 ? 1.0 : 0));

        result.put("relevance", Math.round(baseScore * 10.0) / 10.0);
        result.put("technical", Math.round(tech * 10.0) / 10.0);
        result.put("confidence", Math.round(comm * 10.0) / 10.0);
        result.put("completeness", Math.round(baseScore * 10.0) / 10.0);
        result.put("grammar", Math.round(comm * 10.0) / 10.0);
        result.put("vocabulary", Math.round(comm * 10.0) / 10.0);
        result.put("fluency", Math.round(comm * 10.0) / 10.0);
        if (wordCount < 10) result.put("feedback", "Answer is too short. Please elaborate.");
        else if (tech >= 7) result.put("feedback", "Good technical answer with relevant concepts.");
        else result.put("feedback", "Answer covers basics. Add more technical depth.");
        return result;
    }
}
