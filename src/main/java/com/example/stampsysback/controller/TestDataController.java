package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
public class TestDataController {

    private final UserRepository userRepository;

    public TestDataController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/test-create")
    public String createTestUser() {
        try {
            User u = new User();
            u.setProviderUserId("test-" + System.currentTimeMillis());
            u.setUserName("test-user");
            u.setEmail("test@example.com");
            u.setRole("STUDENT");
            u.setCreatedAt(OffsetDateTime.now());
            User saved = userRepository.save(u);
            return "saved userId=" + saved.getUserId();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "failed: " + ex.getClass().getName() + " - " + ex.getMessage();
        }
    }
}