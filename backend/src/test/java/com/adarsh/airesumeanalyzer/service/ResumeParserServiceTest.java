package com.adarsh.airesumeanalyzer.service;

import com.adarsh.airesumeanalyzer.dto.ResumeParsedResponse;
import com.adarsh.airesumeanalyzer.service.impl.ResumeParserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResumeParserServiceTest {

    private ResumeParserService resumeParserService;

    @BeforeEach
    void setUp() {
        resumeParserService = new ResumeParserServiceImpl();
    }

    @Test
    void parseResume_1_FullResumeParsing_ShouldExtractAllSections() {
        // Given
        String sampleResume = """
                Adarsh Gowda
                adarsh@example.com
                +91 9876543210
                
                SUMMARY
                Experienced Software Engineer with a passion for building scalable web applications.
                
                SKILLS
                Java, Spring Boot, PostgreSQL, React, AWS, Docker
                
                EDUCATION
                B.Tech in Computer Science - VTU
                
                EXPERIENCE
                Senior Developer at Tech Corp
                
                PROJECTS
                AI Resume Analyzer - Spring Boot & React application
                
                CERTIFICATIONS
                AWS Certified Solutions Architect
                """;

        // When
        ResumeParsedResponse response = resumeParserService.parseResume(sampleResume);

        // Then
        assertNotNull(response);
        assertEquals("Adarsh Gowda", response.getName());
        assertEquals("adarsh@example.com", response.getEmail());
        assertTrue(response.getPhone().contains("9876543210"));
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().contains("Software Engineer"));
        assertTrue(response.getSkills().contains("Java"));
        assertTrue(response.getSkills().contains("Spring Boot"));
        assertTrue(response.getSkills().contains("PostgreSQL"));
        assertFalse(response.getEducation().isEmpty());
        assertFalse(response.getExperience().isEmpty());
        assertFalse(response.getProjects().isEmpty());
        assertFalse(response.getCertifications().isEmpty());
    }

    @Test
    void parseResume_2_ContactExtraction_ShouldExtractEmailAndPhone() {
        // Given
        String text = """
                Jane Smith
                Contact: jane.smith99@domain.org | Phone: +1 555-123-4567
                LOCATION: New York, USA
                """;

        // When
        ResumeParsedResponse response = resumeParserService.parseResume(text);

        // Then
        assertNotNull(response);
        assertEquals("jane.smith99@domain.org", response.getEmail());
        assertEquals("+1 555-123-4567", response.getPhone());
    }

    @Test
    void parseResume_3_CaseInsensitiveHeadingDetection_ShouldDetectLowercaseAndMixedHeadings() {
        // Given
        String text = """
                Alice Johnson
                alice@example.com
                
                summary:
                Energetic full stack developer.
                
                technical skills:
                Java, Python, MongoDB
                """;

        // When
        ResumeParsedResponse response = resumeParserService.parseResume(text);

        // Then
        assertNotNull(response);
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().contains("full stack developer"));
        assertTrue(response.getSkills().contains("Java"));
        assertTrue(response.getSkills().contains("Python"));
        assertTrue(response.getSkills().contains("MongoDB"));
    }

    @Test
    void parseResume_4_AlternativeSectionNames_ShouldDetectProfileObjectiveWorkExperience() {
        // Given
        String text = """
                Bob Marley
                bob@example.com
                
                PROFILE
                Passionate software builder.
                
                WORK EXPERIENCE
                Lead Architect at Cloud Inc.
                
                TECHNICAL SKILLS
                TypeScript, Angular, Docker, Kubernetes
                """;

        // When
        ResumeParsedResponse response = resumeParserService.parseResume(text);

        // Then
        assertNotNull(response);
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().contains("Passionate software builder"));
        assertFalse(response.getExperience().isEmpty());
        assertTrue(response.getSkills().contains("TypeScript"));
        assertTrue(response.getSkills().contains("Angular"));
        assertTrue(response.getSkills().contains("Docker"));
    }

    @Test
    void parseResume_5_MissingSections_ShouldReturnNullOrEmptyListsGracefully() {
        // Given
        String text = """
                Charlie Brown
                charlie@example.com
                
                SKILLS
                Git, GitHub, HTML, CSS
                """;

        // When
        ResumeParsedResponse response = resumeParserService.parseResume(text);

        // Then
        assertNotNull(response);
        assertEquals("Charlie Brown", response.getName());
        assertEquals("charlie@example.com", response.getEmail());
        assertNull(response.getSummary());
        assertTrue(response.getEducation().isEmpty());
        assertTrue(response.getExperience().isEmpty());
        assertTrue(response.getProjects().isEmpty());
        assertTrue(response.getCertifications().isEmpty());
        assertTrue(response.getSkills().contains("Git"));
    }

    @Test
    void parseResume_6_UnknownOrUnrecognizedSections_ShouldNotFail() {
        // Given
        String text = """
                David Miller
                david@example.com
                
                HOBBIES & INTERESTS
                Chess, Photography, Travelling
                
                LANGUAGES SPOKEN
                English, Spanish, Hindi
                """;

        // When
        ResumeParsedResponse response = resumeParserService.parseResume(text);

        // Then
        assertNotNull(response);
        assertEquals("David Miller", response.getName());
        assertEquals("david@example.com", response.getEmail());
    }

    @Test
    void parseResume_7_EmptyText_ShouldReturnEmptyResponseWithoutErrors() {
        // When
        ResumeParsedResponse response = resumeParserService.parseResume("   ");

        // Then
        assertNotNull(response);
        assertNull(response.getName());
        assertNull(response.getEmail());
        assertTrue(response.getSkills().isEmpty());
    }

    @Test
    void parseResume_8_NullText_ShouldReturnEmptyResponseWithoutErrors() {
        // When
        ResumeParsedResponse response = resumeParserService.parseResume(null);

        // Then
        assertNotNull(response);
        assertNull(response.getName());
        assertNull(response.getEmail());
        assertTrue(response.getSkills().isEmpty());
    }

    @Test
    void parseResume_9_SkillsMatchedCaseInsensitively_ShouldMatchSkillsInAnyCase() {
        // Given
        String text = """
                Eva Green
                eva@example.com
                
                SKILLS
                java, spring boot, postgresql, machine learning, deep learning, nlp
                """;

        // When
        ResumeParsedResponse response = resumeParserService.parseResume(text);

        // Then
        assertNotNull(response);
        assertTrue(response.getSkills().contains("Java"));
        assertTrue(response.getSkills().contains("Spring Boot"));
        assertTrue(response.getSkills().contains("PostgreSQL"));
        assertTrue(response.getSkills().contains("Machine Learning"));
        assertTrue(response.getSkills().contains("Deep Learning"));
        assertTrue(response.getSkills().contains("NLP"));
    }

    @Test
    void parseResume_10_PartiallyFormattedResume_ShouldNotThrowException() {
        // Given - unstructured/messy text snippet
        String text = """
                Random Unformatted Text Line 1
                Another line without any headers or emails
                just plain text content 12345
                """;

        // When
        ResumeParsedResponse response = assertDoesNotThrow(() -> resumeParserService.parseResume(text));

        // Then
        assertNotNull(response);
        assertNull(response.getEmail());
        assertNull(response.getPhone());
    }
}
