package com.adarsh.airesumeanalyzer.controller;

import com.adarsh.airesumeanalyzer.dto.ResumeParsedResponse;
import com.adarsh.airesumeanalyzer.dto.ResumeResponse;
import com.adarsh.airesumeanalyzer.dto.ResumeUploadResponse;
import com.adarsh.airesumeanalyzer.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /**
     * Retrieves all resumes belonging to the currently authenticated user.
     *
     * @param authentication the current authentication principal (injected by Spring Security)
     * @return List of ResumeResponse with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getUserResumes(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ResumeResponse> resumes = resumeService.getUserResumes(userEmail);
        return ResponseEntity.ok(resumes);
    }

    /**
     * Retrieves a specific resume by ID belonging to the currently authenticated user.
     *
     * @param id             the resume ID (path variable)
     * @param authentication the current authentication principal (injected by Spring Security)
     * @return ResumeResponse with HTTP 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ResumeResponse response = resumeService.getResumeById(id, userEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific resume by ID belonging to the currently authenticated user.
     *
     * @param id             the resume ID (path variable)
     * @param authentication the current authentication principal (injected by Spring Security)
     * @return ResponseEntity with HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        resumeService.deleteResume(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    /**
     * Parses a specific resume by ID belonging to the currently authenticated user.
     *
     * @param id             the resume ID (path variable)
     * @param authentication the current authentication principal (injected by Spring Security)
     * @return ResumeParsedResponse with HTTP 200 OK
     */
    @GetMapping("/{id}/parse")
    public ResponseEntity<ResumeParsedResponse> parseResume(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ResumeParsedResponse response = resumeService.parseResume(id, userEmail);
        return ResponseEntity.ok(response);
    }
}
