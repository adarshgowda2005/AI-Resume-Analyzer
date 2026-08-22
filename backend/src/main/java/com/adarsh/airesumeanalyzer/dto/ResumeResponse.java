package com.adarsh.airesumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing resume details returned in list/fetch operations.
 * Excludes sensitive user information such as passwords.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeResponse {

    private Long id;
    private String originalFileName;
    private String storedFileName;
    private LocalDateTime uploadedAt;
    private String uploadedByName;
    private String uploadedByEmail;
}
