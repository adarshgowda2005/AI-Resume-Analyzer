package com.adarsh.airesumeanalyzer.controller;

import com.adarsh.airesumeanalyzer.dto.ResumeUploadResponse;
import com.adarsh.airesumeanalyzer.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller handling resume management endpoints.
 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Uploads a resume PDF file for the currently authenticated user.
     * The authenticated user is extracted from the JWT SecurityContext.
     *
     * @param file           the PDF file to upload (multipart/form-data)
     * @param authentication the current authentication principal (injected by Spring Security)
     * @return ResumeUploadResponse with HTTP 201 Created
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ResumeUploadResponse response = resumeService.uploadResume(file, userEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
