package com.gulshan.hiresenseai.config;

import com.gulshan.hiresenseai.entity.User;
import com.gulshan.hiresenseai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.mindrot.jbcrypt.BCrypt;

@Configuration
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default ADMIN if not exists
        if (!userRepository.existsByEmail("admin@hiresense.ai")) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@hiresense.ai");
            admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("[DB Init] Created default admin user: admin@hiresense.ai / admin123");
        }

        // Create default RECRUITER if not exists
        if (!userRepository.existsByEmail("recruiter@hiresense.ai")) {
            User recruiter = new User();
            recruiter.setName("Hiring Manager");
            recruiter.setEmail("recruiter@hiresense.ai");
            recruiter.setPassword(BCrypt.hashpw("recruiter123", BCrypt.gensalt()));
            recruiter.setRole(User.Role.RECRUITER);
            userRepository.save(recruiter);
            System.out.println("[DB Init] Created default recruiter user: recruiter@hiresense.ai / recruiter123");
        }
    }
}
