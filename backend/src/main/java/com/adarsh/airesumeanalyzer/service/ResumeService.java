package com.adarsh.airesumeanalyzer.service;

import com.adarsh.airesumeanalyzer.dto.ResumeParsedResponse;
import com.adarsh.airesumeanalyzer.dto.ResumeResponse;
import com.adarsh.airesumeanalyzer.dto.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /**
     * Retrieves all resumes uploaded by the specified user.
     *
     * @param userEmail the email of the authenticated user
     * @return list of ResumeResponse objects belonging to the user
     */
    List<ResumeResponse> getUserResumes(String userEmail);

    /**
     * Retrieves a specific resume by ID for the specified user.
     *
     * @param id        the ID of the resume to retrieve
     * @param userEmail the email of the authenticated user
     * @return ResumeResponse containing the resume details
     */
    ResumeResponse getResumeById(Long id, String userEmail);

    /**
     * Deletes a specific resume by ID for the specified user and removes its physical file.
     *
     * @param id        the ID of the resume to delete
     * @param userEmail the email of the authenticated user
     */
    void deleteResume(Long id, String userEmail);

    /**
     * Parses the specified resume by ID for the authenticated user, extracting structured resume data.
     *
     * @param id        the ID of the resume to parse
     * @param userEmail the email of the authenticated user
     * @return ResumeParsedResponse containing structured resume details
     */
    ResumeParsedResponse parseResume(Long id, String userEmail);
}
