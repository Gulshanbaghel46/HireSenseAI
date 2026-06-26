package com.gulshan.hiresenseai.controller;

import com.gulshan.hiresenseai.entity.User;
import com.gulshan.hiresenseai.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.mindrot.jbcrypt.BCrypt;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired private UserRepository userRepository;

    private boolean isLoggedIn(HttpSession s) { return s.getAttribute("userId") != null; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/registerForm")
    public String registerForm(@RequestParam String name, @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam(defaultValue = "CANDIDATE") String role,
                               Model model) {
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email already registered. Please login.");
            return "register";
        }
        User user = new User();
        user.setName(name); user.setEmail(email); user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        try { user.setRole(User.Role.valueOf(role)); } catch (Exception e) { user.setRole(User.Role.CANDIDATE); }
        userRepository.save(user);
        model.addAttribute("message", "Account created successfully! Please login.");
        return "success";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/loginForm")
    public String loginForm(@RequestParam String email, @RequestParam String password,
                            HttpSession session, Model model) {
        User user = userRepository.findByEmail(email);
        boolean passwordMatches = false;
        if (user != null && user.getPassword() != null) {
            try {
                passwordMatches = BCrypt.checkpw(password, user.getPassword());
            } catch (IllegalArgumentException e) {
                // Fallback for existing plain text passwords
                passwordMatches = user.getPassword().equals(password);
                if (passwordMatches) {
                    user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                    userRepository.save(user);
                }
            }
        }
        if (passwordMatches) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole().name());
            return switch (user.getRole()) {
                case ADMIN     -> "redirect:/admin/dashboard";
                case RECRUITER -> "redirect:/recruiter/dashboard";
                default        -> "redirect:/users/dashboard";
            };
        }
        model.addAttribute("error", "Invalid email or password. Please try again.");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";
        model.addAttribute("name", session.getAttribute("userName"));
        model.addAttribute("role", session.getAttribute("userRole"));
        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/users/login";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam String name, @RequestParam String email,
                                HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/users/login";
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setName(name); user.setEmail(email);
            userRepository.save(user);
            session.setAttribute("userName", name);
            session.setAttribute("userEmail", email);
        }
        model.addAttribute("message", "Profile updated successfully.");
        model.addAttribute("user", user);
        return "profile";
    }
}