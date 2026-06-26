package com.gulshan.hiresenseai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class E2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testFullFlow() {
        // 1. Home Page
        ResponseEntity<String> homeResp = restTemplate.getForEntity("/", String.class);
        assertEquals(HttpStatus.FOUND, homeResp.getStatusCode());

        String uniqueEmail = "test" + System.currentTimeMillis() + "@test.com";
        // 2. Register User
        MultiValueMap<String, String> regMap = new LinkedMultiValueMap<>();
        regMap.add("name", "Test User");
        regMap.add("email", uniqueEmail);
        regMap.add("password", "password123");
        regMap.add("role", "CANDIDATE");
        ResponseEntity<String> regResp = restTemplate.postForEntity("/users/registerForm", regMap, String.class);
        assertEquals(HttpStatus.OK, regResp.getStatusCode());
        assertTrue(regResp.getBody().contains("successfully"));

        // 3. Login
        MultiValueMap<String, String> loginMap = new LinkedMultiValueMap<>();
        loginMap.add("email", uniqueEmail);
        loginMap.add("password", "password123");
        ResponseEntity<String> loginResp = restTemplate.postForEntity("/users/loginForm", loginMap, String.class);
        assertEquals(HttpStatus.FOUND, loginResp.getStatusCode()); // Redirects to dashboard

        // Verify it redirects to dashboard
        String location = loginResp.getHeaders().getLocation().toString();
        assertTrue(location.contains("/users/dashboard"));
        
        // Get the session cookie
        String cookie = loginResp.getHeaders().getFirst("Set-Cookie");
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (cookie != null) {
            headers.add("Cookie", cookie);
        }
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(null, headers);
        
        // 4. Test Report Page
        ResponseEntity<String> interviewResp = restTemplate.exchange("/users/interview", org.springframework.http.HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, interviewResp.getStatusCode());
        
        // Extract sessionId from the HTML (it contains <input type="hidden" name="sessionId" value="...">)
        String html = interviewResp.getBody();
        String sessionId = null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("name=\"sessionId\" value=\"([^\"]+)\"").matcher(html);
        if (m.find()) {
            sessionId = m.group(1);
        }
        
        if (sessionId != null) {
            ResponseEntity<String> reportResp = restTemplate.exchange("/users/report?sessionId=" + sessionId, org.springframework.http.HttpMethod.GET, entity, String.class);
            if (reportResp.getStatusCode() != HttpStatus.OK) {
                System.out.println("REPORT FAILED WITH: " + reportResp.getStatusCode());
                System.out.println(reportResp.getBody());
            }
            assertEquals(HttpStatus.OK, reportResp.getStatusCode());
        }
    }
}
