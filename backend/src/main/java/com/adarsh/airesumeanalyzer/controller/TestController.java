package com.adarsh.airesumeanalyzer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller providing protected endpoints to verify JWT authentication context.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Protected profile endpoint returning authenticated user email and access message.
     *
     * @return ResponseEntity containing JSON payload with message and user email
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (authentication != null) ? authentication.getName() : "Anonymous";

        Map<String, String> response = new HashMap<>();
        response.put("message", "Protected profile accessed successfully");
        response.put("email", userEmail);

        return ResponseEntity.ok(response);
    }
}
