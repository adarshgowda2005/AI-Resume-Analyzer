package com.adarsh.airesumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing parsed education details from a resume.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationResponse {

    private String institution;
    private String degree;
    private String fieldOfStudy;
    private String startDate;
    private String endDate;
}
