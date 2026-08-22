package com.adarsh.airesumeanalyzer.service;

import com.adarsh.airesumeanalyzer.dto.ResumeParsedResponse;

/**
 * Service interface for parsing plain text extracted from PDF resumes into a structured object.
 */
public interface ResumeParserService {

    /**
     * Parses the provided raw resume text and converts it into a structured {@link ResumeParsedResponse}.
     *
     * @param text raw text extracted from a resume PDF
     * @return structured ResumeParsedResponse
     */
    ResumeParsedResponse parseResume(String text);
}
