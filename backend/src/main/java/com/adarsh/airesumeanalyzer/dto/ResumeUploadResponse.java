package com.adarsh.airesumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the response after a successful resume upload.
 * Excludes sensitive user fields such as password.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeUploadResponse {

    private Long id;
    private String originalFileName;
    private String storedFileName;
    private LocalDateTime uploadedAt;
    private String message;

    // Safe user info — no password or sensitive fields exposed
    private String uploadedByName;
    private String uploadedByEmail;
}
