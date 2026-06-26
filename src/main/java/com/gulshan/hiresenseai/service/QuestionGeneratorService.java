package com.gulshan.hiresenseai.service;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * QuestionGeneratorService — generates tailored interview questions based on
 * job role and skills detected in the candidate's resume.
 */
@Service
public class QuestionGeneratorService {

    // ─── Role-based question banks ─────────────────────────────────────────────

    private static final Map<String, List<String>> ROLE_QUESTIONS = new HashMap<>();

    static {
        ROLE_QUESTIONS.put("java", Arrays.asList(
            "Explain the four pillars of Object-Oriented Programming with examples.",
            "What is the difference between JDK, JRE, and JVM?",
            "How does Java's garbage collection work?",
            "Explain the difference between ArrayList and LinkedList.",
            "What are Java Streams and how do you use them?",
            "What is the difference between checked and unchecked exceptions?",
            "Explain the concept of multithreading in Java.",
            "What is Spring Boot and what problem does it solve?",
            "How does dependency injection work in Spring?",
            "Explain the difference between @Component, @Service, and @Repository.",
            "What is a RESTful API and how do you build one in Spring Boot?",
            "Explain SOLID principles and how they apply to Java development.",
            "What is the difference between an interface and an abstract class?",
            "How does JPA/Hibernate simplify database operations?",
            "What are design patterns? Name and explain 3 commonly used ones."
        ));

        ROLE_QUESTIONS.put("ai", Arrays.asList(
            "Explain the difference between supervised, unsupervised, and reinforcement learning.",
            "What is overfitting and how do you prevent it?",
            "Explain the backpropagation algorithm in neural networks.",
            "What is the difference between precision, recall, and F1 score?",
            "Explain how a Convolutional Neural Network (CNN) works.",
            "What are transformers and how do they differ from RNNs?",
            "What is transfer learning and when would you use it?",
            "Explain the bias-variance tradeoff.",
            "What is regularization? Explain L1 and L2 regularization.",
            "How does the gradient descent algorithm work?",
            "Explain the concept of embeddings in NLP.",
            "What is a generative adversarial network (GAN)?",
            "How would you handle imbalanced datasets?",
            "What is cross-validation and why is it important?",
            "Explain the attention mechanism in transformers."
        ));

        ROLE_QUESTIONS.put("web", Arrays.asList(
            "Explain the difference between HTTP and HTTPS.",
            "What is the DOM and how does JavaScript interact with it?",
            "Explain CSS flexbox and grid layouts with use cases.",
            "What is React and what problem does it solve?",
            "Explain the virtual DOM concept in React.",
            "What are React hooks? Explain useState and useEffect.",
            "What is REST vs GraphQL? When would you use each?",
            "Explain the concept of responsive design.",
            "What is a closure in JavaScript?",
            "Explain async/await and Promises in JavaScript.",
            "What is CORS and how do you handle it?",
            "Explain the difference between localStorage and sessionStorage.",
            "What is webpack and why is it used?",
            "Describe the browser's critical rendering path.",
            "What are web accessibility best practices?"
        ));

        ROLE_QUESTIONS.put("data", Arrays.asList(
            "What is the difference between a data warehouse and a data lake?",
            "Explain the ETL process.",
            "What is the difference between OLTP and OLAP systems?",
            "How do you handle missing data in a dataset?",
            "Explain normalization in databases and its normal forms.",
            "What are window functions in SQL? Give examples.",
            "What is Apache Spark and when would you use it?",
            "Explain the CAP theorem.",
            "What is a star schema vs snowflake schema?",
            "How would you design a data pipeline for real-time analytics?",
            "What is Pandas and how do you use it for data manipulation?",
            "Explain feature engineering and its importance in ML.",
            "How would you optimize a slow SQL query?",
            "What is A/B testing and how do you analyze results?",
            "Explain the concept of data partitioning."
        ));

        ROLE_QUESTIONS.put("devops", Arrays.asList(
            "What is CI/CD and why is it important?",
            "Explain the difference between Docker and virtual machines.",
            "What is Kubernetes and what problem does it solve?",
            "Explain the concept of infrastructure as code (IaC).",
            "What is the difference between horizontal and vertical scaling?",
            "Explain blue-green deployment strategy.",
            "What are microservices and how do they differ from monoliths?",
            "How does a load balancer work?",
            "What is Terraform and how does it work?",
            "Explain the 12-factor app methodology.",
            "What is a service mesh and when would you use Istio?",
            "How do you monitor applications in production?",
            "Explain the concept of chaos engineering.",
            "What is GitOps and how does it relate to Kubernetes?",
            "How do you handle secrets management in a cloud environment?"
        ));

        ROLE_QUESTIONS.put("cloud", Arrays.asList(
            "Explain the differences between IaaS, PaaS, and SaaS.",
            "What are the main AWS/GCP/Azure services you've worked with?",
            "How does auto-scaling work in cloud environments?",
            "What is serverless computing and when would you use it?",
            "Explain the concept of a CDN and its benefits.",
            "What is the difference between S3, EBS, and EFS in AWS?",
            "How do you secure a cloud infrastructure?",
            "What is VPC and how does subnetting work in AWS?",
            "Explain the shared responsibility model in cloud security.",
            "What are Lambda functions and their limitations?",
            "How do you optimize cloud costs?",
            "What is the difference between RDS and DynamoDB?",
            "Explain how Route 53 DNS works.",
            "What is CloudWatch and how do you set up alerts?",
            "How do you implement disaster recovery in the cloud?"
        ));

        // Default / General questions
        ROLE_QUESTIONS.put("general", Arrays.asList(
            "Tell me about yourself and your technical background.",
            "Describe a challenging project you worked on and how you solved it.",
            "What is your greatest technical strength?",
            "How do you keep your technical skills up to date?",
            "Describe a situation where you had to debug a complex issue.",
            "How do you approach learning new technologies?",
            "What is your experience with version control systems like Git?",
            "Describe your experience working in a team.",
            "How do you handle tight deadlines?",
            "Where do you see yourself in 5 years?"
        ));
    }

    // ─── Skill-based question additions ───────────────────────────────────────

    private static final Map<String, List<String>> SKILL_QUESTIONS = new HashMap<>();

    static {
        SKILL_QUESTIONS.put("sql", Arrays.asList(
            "Write a SQL query to find the second highest salary from an employee table.",
            "Explain the difference between INNER JOIN, LEFT JOIN, and FULL OUTER JOIN.",
            "What is an index in a database and how does it improve performance?"
        ));
        SKILL_QUESTIONS.put("python", Arrays.asList(
            "What is the difference between a list and a tuple in Python?",
            "Explain Python's GIL (Global Interpreter Lock).",
            "How do Python decorators work?"
        ));
        SKILL_QUESTIONS.put("docker", Arrays.asList(
            "What is the difference between a Docker image and a container?",
            "Explain Docker Compose and its use cases.",
            "How do you optimize a Docker image for production?"
        ));
        SKILL_QUESTIONS.put("git", Arrays.asList(
            "Explain the difference between git merge and git rebase.",
            "How do you resolve a merge conflict?",
            "What is git flow branching strategy?"
        ));
        SKILL_QUESTIONS.put("react", Arrays.asList(
            "What is the difference between controlled and uncontrolled components in React?",
            "How does React's reconciliation algorithm work?",
            "Explain the useReducer hook and when to prefer it over useState."
        ));
        SKILL_QUESTIONS.put("machine learning", Arrays.asList(
            "What is the difference between classification and regression?",
            "How does a Random Forest algorithm work?",
            "Explain k-means clustering."
        ));
    }

    /**
     * Generate a list of questions for a given job role, optionally boosted
     * with skill-specific questions detected from the resume.
     */
    public List<String> generateQuestions(String jobRole, List<String> detectedSkills, int count) {
        List<String> questions = new ArrayList<>();
        String normalizedRole = normalizeRole(jobRole);

        // Get role-based questions
        List<String> roleBank = new ArrayList<>(
            ROLE_QUESTIONS.getOrDefault(normalizedRole, ROLE_QUESTIONS.get("general"))
        );
        Collections.shuffle(roleBank);

        // Add skill-specific questions
        if (detectedSkills != null) {
            for (String skill : detectedSkills) {
                List<String> skillBank = SKILL_QUESTIONS.get(skill.toLowerCase());
                if (skillBank != null) {
                    questions.addAll(skillBank.subList(0, Math.min(2, skillBank.size())));
                }
            }
        }

        // Fill remaining slots from role bank
        int remaining = count - questions.size();
        for (int i = 0; i < Math.min(remaining, roleBank.size()); i++) {
            if (!questions.contains(roleBank.get(i))) {
                questions.add(roleBank.get(i));
            }
        }

        // Ensure we don't exceed count
        if (questions.size() > count) {
            questions = questions.subList(0, count);
        }

        // Fallback if still not enough
        if (questions.isEmpty()) {
            List<String> general = new ArrayList<>(ROLE_QUESTIONS.get("general"));
            Collections.shuffle(general);
            return general.subList(0, Math.min(count, general.size()));
        }

        return questions;
    }

    private String normalizeRole(String role) {
        if (role == null) return "general";
        String lower = role.toLowerCase();
        if (lower.contains("java")) return "java";
        if (lower.contains("ai") || lower.contains("ml") || lower.contains("machine")) return "ai";
        if (lower.contains("web") || lower.contains("frontend") || lower.contains("fullstack")) return "web";
        if (lower.contains("data")) return "data";
        if (lower.contains("devops")) return "devops";
        if (lower.contains("cloud")) return "cloud";
        return "general";
    }

    public List<String> getAvailableRoles() {
        return Arrays.asList(
            "Java Developer", "AI/ML Engineer", "Web Developer",
            "Data Scientist", "DevOps Engineer", "Cloud Engineer"
        );
    }
}
