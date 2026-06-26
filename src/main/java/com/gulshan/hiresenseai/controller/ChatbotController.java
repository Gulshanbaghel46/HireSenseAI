package com.gulshan.hiresenseai.controller;

import com.gulshan.hiresenseai.service.AIEvaluationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/users/chatbot")
public class ChatbotController {

    @Autowired
    private AIEvaluationService aiEvaluationService;

    private boolean isLoggedIn(HttpSession s) {
        return s.getAttribute("userId") != null;
    }

    @GetMapping
    public String chatbotPage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        model.addAttribute("name", session.getAttribute("userName"));
        return "chatbot";
    }

    @PostMapping("/message")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chatMessage(@RequestBody Map<String, String> payload, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        if (!isLoggedIn(session)) {
            response.put("error", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        String userMessage = payload.get("message");
        String chatHistory = payload.get("history");
        String jobRole = payload.get("jobRole");
        if (jobRole == null || jobRole.isBlank()) {
            jobRole = "Software Engineer";
        }

        String aiResponse = aiEvaluationService.generateFollowUpQuestion(chatHistory, userMessage, jobRole);
        if (aiResponse == null || aiResponse.isBlank()) {
            aiResponse = "Interesting point. Can you elaborate further on how you would handle scalability and error handling in that context?";
        }

        response.put("reply", aiResponse);
        return ResponseEntity.ok(response);
    }
}
