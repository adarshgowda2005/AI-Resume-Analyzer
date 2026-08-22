package com.adarsh.airesumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing the complete structured parsed resume result.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeParsedResponse {

    private String name;
    private String email;
    private String phone;
    private String summary;
    private List<String> skills;
    private List<EducationResponse> education;
    private List<ExperienceResponse> experience;
    private List<ProjectResponse> projects;
    private List<CertificationResponse> certifications;
}
