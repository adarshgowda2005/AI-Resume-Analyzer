package com.adarsh.airesumeanalyzer.service;

import com.adarsh.airesumeanalyzer.dto.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for resume management operations.
 */
public interface ResumeService {

    /**
     * Uploads a resume PDF file for the currently authenticated user.
     *
     * @param file      the multipart PDF file to upload
     * @param userEmail the email of the authenticated user (from JWT/SecurityContext)
     * @return ResumeUploadResponse containing upload metadata
     */
    ResumeUploadResponse uploadResume(MultipartFile file, String userEmail);
}
