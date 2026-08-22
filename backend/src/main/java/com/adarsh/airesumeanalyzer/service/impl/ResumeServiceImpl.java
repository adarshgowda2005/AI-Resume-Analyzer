package com.adarsh.airesumeanalyzer.service.impl;

import com.adarsh.airesumeanalyzer.dto.ResumeParsedResponse;
import com.adarsh.airesumeanalyzer.dto.ResumeResponse;
import com.adarsh.airesumeanalyzer.dto.ResumeUploadResponse;
import com.adarsh.airesumeanalyzer.entity.Resume;
import com.adarsh.airesumeanalyzer.entity.User;
import com.adarsh.airesumeanalyzer.exception.ResourceNotFoundException;
import com.adarsh.airesumeanalyzer.repository.ResumeRepository;
import com.adarsh.airesumeanalyzer.repository.UserRepository;
import com.adarsh.airesumeanalyzer.service.PdfTextExtractionService;
import com.adarsh.airesumeanalyzer.service.ResumeParserService;
import com.adarsh.airesumeanalyzer.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ResumeService} for handling resume PDF uploads, retrieval, deletion, and parsing.
 */
@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PdfTextExtractionService pdfTextExtractionService;
    private final ResumeParserService resumeParserService;
    private final Path uploadDirectory;

    public ResumeServiceImpl(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            PdfTextExtractionService pdfTextExtractionService,
            ResumeParserService resumeParserService,
            @Value("${app.upload.dir:uploads/resumes}") String uploadDir
    ) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.resumeParserService = resumeParserService;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        createUploadDirectory();
    }

    /**
     * Creates the upload directory if it does not already exist.
     */
    private void createUploadDirectory() {
        try {
            Files.createDirectories(uploadDirectory);
            logger.info("Upload directory ready: {}", uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDirectory, e);
        }
    }

    @Override
    public ResumeUploadResponse uploadResume(MultipartFile file, String userEmail) {
        // Validate file is not empty
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Cannot upload an empty file. Please select a PDF file.");
        }

        // Validate original filename extension (.pdf)
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF files are accepted. Filename must end with .pdf extension.");
        }

        // Validate PDF magic bytes header (%PDF signature: 0x25 0x50 0x44 0x46)
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[4];
            int bytesRead = inputStream.read(header);
            if (bytesRead < 4 || header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                throw new RuntimeException("Invalid PDF file content. The uploaded file is not a valid PDF document.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content during PDF validation.", e);
        }

        // Look up the authenticated user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));

        // Generate a unique stored filename to prevent collisions
        String storedFileName = UUID.randomUUID().toString() + ".pdf";
        Path targetPath = uploadDirectory.resolve(storedFileName);

        try {
            // Save the file to disk
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Resume saved to: {}", targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store resume file. Please try again.", e);
        }

        // Save metadata to database
        Resume resume = Resume.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .filePath(targetPath.toString())
                .user(user)
                .build();

        Resume savedResume = resumeRepository.save(resume);
        logger.info("Resume metadata saved with id: {} for user: {}", savedResume.getId(), userEmail);

        return ResumeUploadResponse.builder()
                .id(savedResume.getId())
                .originalFileName(savedResume.getOriginalFileName())
                .storedFileName(savedResume.getStoredFileName())
                .uploadedAt(savedResume.getUploadedAt())
                .uploadedByName(user.getFullName())
                .uploadedByEmail(user.getEmail())
                .message("Resume uploaded successfully")
                .build();
    }

    @Override
    public List<ResumeResponse> getUserResumes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));

        List<Resume> resumes = resumeRepository.findByUserOrderByUploadedAtDesc(user);
        logger.info("Retrieved {} resume(s) for user: {}", resumes.size(), userEmail);

        return resumes.stream()
                .map(resume -> ResumeResponse.builder()
                        .id(resume.getId())
                        .originalFileName(resume.getOriginalFileName())
                        .storedFileName(resume.getStoredFileName())
                        .uploadedAt(resume.getUploadedAt())
                        .uploadedByName(user.getFullName())
                        .uploadedByEmail(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ResumeResponse getResumeById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));

        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        logger.info("Retrieved resume with id: {} for user: {}", id, userEmail);

        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .storedFileName(resume.getStoredFileName())
                .uploadedAt(resume.getUploadedAt())
                .uploadedByName(user.getFullName())
                .uploadedByEmail(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void deleteResume(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));

        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        // Prevent path traversal
        String storedFileName = resume.getStoredFileName();
        Path targetPath = uploadDirectory.resolve(storedFileName).normalize();
        if (!targetPath.startsWith(uploadDirectory)) {
            throw new SecurityException("Invalid file path: path traversal detected");
        }

        // Physical file deletion
        try {
            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) {
                logger.info("Physical resume file deleted: {}", targetPath);
            } else {
                logger.warn("Physical resume file missing on disk at: {}. Proceeding with database record deletion.", targetPath);
            }
        } catch (IOException e) {
            logger.error("Failed to delete physical resume file at: {}", targetPath, e);
        }

        // Database record deletion
        resumeRepository.delete(resume);
        logger.info("Resume database record deleted with id: {} for user: {}", id, userEmail);
    }

    @Override
    public ResumeParsedResponse parseResume(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));

        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        String extractedText = pdfTextExtractionService.extractText(resume.getFilePath());
        ResumeParsedResponse parsedResponse = resumeParserService.parseResume(extractedText);

        logger.info("Successfully parsed resume id: {} for user: {}", id, userEmail);
        return parsedResponse;
    }
}
