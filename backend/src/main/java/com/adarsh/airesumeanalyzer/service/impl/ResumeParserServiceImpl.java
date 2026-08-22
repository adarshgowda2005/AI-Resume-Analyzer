package com.adarsh.airesumeanalyzer.service.impl;

import com.adarsh.airesumeanalyzer.dto.CertificationResponse;
import com.adarsh.airesumeanalyzer.dto.EducationResponse;
import com.adarsh.airesumeanalyzer.dto.ExperienceResponse;
import com.adarsh.airesumeanalyzer.dto.ProjectResponse;
import com.adarsh.airesumeanalyzer.dto.ResumeParsedResponse;
import com.adarsh.airesumeanalyzer.service.ResumeParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Deterministic, rule-based implementation of {@link ResumeParserService}.
 */
@Service
public class ResumeParserServiceImpl implements ResumeParserService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParserServiceImpl.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}|\\+?\\d{10,12}");

    // Controlled tech skill dictionary
    private static final List<String> TECH_SKILL_DICTIONARY = Arrays.asList(
            "Java", "Python", "C", "C++", "JavaScript", "TypeScript",
            "Spring", "Spring Boot", "Hibernate", "REST API", "JPA",
            "HTML", "CSS", "React", "Angular",
            "PostgreSQL", "MySQL", "MongoDB", "SQL",
            "AWS", "Azure", "Docker", "Kubernetes", "Jenkins", "Git", "GitHub",
            "Machine Learning", "Deep Learning", "NLP", "TensorFlow", "PyTorch", "Pandas", "NumPy", "Scikit-learn"
    );

    // Known Section Aliases
    private static final Map<String, List<String>> SECTION_ALIASES = new HashMap<>();

    static {
        SECTION_ALIASES.put("SUMMARY", Arrays.asList("SUMMARY", "PROFILE", "OBJECTIVE", "EXECUTIVE SUMMARY", "PROFESSIONAL SUMMARY", "ABOUT ME", "CAREER OBJECTIVE"));
        SECTION_ALIASES.put("EXPERIENCE", Arrays.asList("EXPERIENCE", "WORK EXPERIENCE", "EMPLOYMENT HISTORY", "PROFESSIONAL EXPERIENCE", "WORK HISTORY", "EMPLOYMENT"));
        SECTION_ALIASES.put("EDUCATION", Arrays.asList("EDUCATION", "ACADEMIC BACKGROUND", "ACADEMICS", "QUALIFICATIONS", "EDUCATIONAL QUALIFICATIONS"));
        SECTION_ALIASES.put("SKILLS", Arrays.asList("SKILLS", "TECHNICAL SKILLS", "CORE COMPETENCIES", "SKILLS & EXPERTISE", "TECHNOLOGIES", "KEY SKILLS"));
        SECTION_ALIASES.put("PROJECTS", Arrays.asList("PROJECTS", "KEY PROJECTS", "ACADEMIC PROJECTS", "PERSONAL PROJECTS"));
        SECTION_ALIASES.put("CERTIFICATIONS", Arrays.asList("CERTIFICATIONS", "CERTIFICATES", "LICENSES & CERTIFICATIONS", "TRAINING & CERTIFICATIONS", "ACHIEVEMENTS", "HONORS & AWARDS"));
    }

    @Override
    public ResumeParsedResponse parseResume(String text) {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Received empty or null text for resume parsing.");
            return createEmptyResponse();
        }

        String normalizedText = text.replace("\r\n", "\n").replace("\r", "\n");
        List<String> lines = Arrays.stream(normalizedText.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        if (lines.isEmpty()) {
            return createEmptyResponse();
        }

        String email = extractEmail(normalizedText);
        String phone = extractPhone(normalizedText);
        String name = extractName(lines, email, phone);

        Map<String, List<String>> sections = extractSections(lines);
        List<String> detectedSkills = extractSkills(normalizedText, sections.get("SKILLS"));

        String summary = sections.containsKey("SUMMARY") ? String.join(" ", sections.get("SUMMARY")) : null;
        List<EducationResponse> educationList = parseEducation(sections.get("EDUCATION"));
        List<ExperienceResponse> experienceList = parseExperience(sections.get("EXPERIENCE"));
        List<ProjectResponse> projectList = parseProjects(sections.get("PROJECTS"));
        List<CertificationResponse> certificationList = parseCertifications(sections.get("CERTIFICATIONS"));

        logger.info("Successfully parsed resume text [sections detected: {}, skills detected: {}, character count: {}]",
                sections.size(), detectedSkills.size(), normalizedText.length());

        return ResumeParsedResponse.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .summary(summary)
                .skills(detectedSkills)
                .education(educationList)
                .experience(experienceList)
                .projects(projectList)
                .certifications(certificationList)
                .build();
    }

    private ResumeParsedResponse createEmptyResponse() {
        return ResumeParsedResponse.builder()
                .name(null)
                .email(null)
                .phone(null)
                .summary(null)
                .skills(Collections.emptyList())
                .education(Collections.emptyList())
                .experience(Collections.emptyList())
                .projects(Collections.emptyList())
                .certifications(Collections.emptyList())
                .build();
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            String candidate = matcher.group().trim();
            if (candidate.replaceAll("\\D", "").length() >= 10) {
                return candidate;
            }
        }
        return null;
    }

    private String extractName(List<String> lines, String email, String phone) {
        for (int i = 0; i < Math.min(lines.size(), 5); i++) {
            String line = lines.get(i);
            if (isSectionHeading(line) != null) {
                break;
            }
            if (email != null && line.contains(email)) {
                continue;
            }
            if (phone != null && line.contains(phone)) {
                continue;
            }
            if (line.toLowerCase().contains("resume") || line.toLowerCase().contains("curriculum vitae")) {
                continue;
            }
            // Candidate name line heuristic (letters, spaces, dots, 2 to 4 words)
            if (line.matches("^[A-Za-z\\s.]{2,40}$") && line.split("\\s+").length >= 1) {
                return line;
            }
        }
        return null;
    }

    private Map<String, List<String>> extractSections(List<String> lines) {
        Map<String, List<String>> sectionMap = new HashMap<>();
        String currentSection = null;

        for (String line : lines) {
            String detectedHeading = isSectionHeading(line);
            if (detectedHeading != null) {
                currentSection = detectedHeading;
                sectionMap.putIfAbsent(currentSection, new ArrayList<>());
            } else if (currentSection != null) {
                sectionMap.get(currentSection).add(line);
            }
        }
        return sectionMap;
    }

    private String isSectionHeading(String line) {
        String cleanLine = line.replaceAll("[:#_-]", "").trim().toUpperCase();
        for (Map.Entry<String, List<String>> entry : SECTION_ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                if (cleanLine.equals(alias)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private List<String> extractSkills(String fullText, List<String> skillSectionLines) {
        Set<String> foundSkills = new LinkedHashSet<>();
        String combinedText = fullText + " " + (skillSectionLines != null ? String.join(" ", skillSectionLines) : "");

        for (String skill : TECH_SKILL_DICTIONARY) {
            Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(skill) + "\\b");
            if (pattern.matcher(combinedText).find()) {
                foundSkills.add(skill);
            }
        }

        return new ArrayList<>(foundSkills);
    }

    private List<EducationResponse> parseEducation(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<EducationResponse> list = new ArrayList<>();
        for (String line : lines) {
            list.add(EducationResponse.builder()
                    .degree(line)
                    .institution(null)
                    .fieldOfStudy(null)
                    .startDate(null)
                    .endDate(null)
                    .build());
        }
        return list;
    }

    private List<ExperienceResponse> parseExperience(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExperienceResponse> list = new ArrayList<>();
        for (String line : lines) {
            list.add(ExperienceResponse.builder()
                    .jobTitle(line)
                    .company(null)
                    .startDate(null)
                    .endDate(null)
                    .description(line)
                    .build());
        }
        return list;
    }

    private List<ProjectResponse> parseProjects(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<ProjectResponse> list = new ArrayList<>();
        for (String line : lines) {
            list.add(ProjectResponse.builder()
                    .name(line)
                    .description(line)
                    .technologies(Collections.emptyList())
                    .build());
        }
        return list;
    }

    private List<CertificationResponse> parseCertifications(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<CertificationResponse> list = new ArrayList<>();
        for (String line : lines) {
            list.add(CertificationResponse.builder()
                    .name(line)
                    .issuer(null)
                    .date(null)
                    .build());
        }
        return list;
    }
}
