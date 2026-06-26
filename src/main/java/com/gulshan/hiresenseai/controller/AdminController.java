package com.gulshan.hiresenseai.controller;

import com.gulshan.hiresenseai.entity.User;
import com.gulshan.hiresenseai.repository.UserRepository;
import com.gulshan.hiresenseai.repository.InterviewSessionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewSessionRepository sessionRepository;

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        return "ADMIN".equals(role);
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/users/login";
        }
        
        List<User> users = userRepository.findAll();
        long totalInterviews = sessionRepository.count();
        
        model.addAttribute("users", users);
        model.addAttribute("totalInterviews", totalInterviews);
        model.addAttribute("name", session.getAttribute("userName"));
        return "admin";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam Long userId, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/users/login";
        }
        userRepository.deleteById(userId);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/updateUserRole")
    public String updateUserRole(@RequestParam Long userId, @RequestParam String role, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/users/login";
        }
        userRepository.findById(userId).ifPresent(user -> {
            try {
                user.setRole(User.Role.valueOf(role));
                userRepository.save(user);
            } catch (IllegalArgumentException e) {
                // Ignore invalid roles
            }
        });
        return "redirect:/admin/dashboard";
    }
}
