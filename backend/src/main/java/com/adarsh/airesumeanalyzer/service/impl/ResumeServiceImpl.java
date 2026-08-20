package com.adarsh.airesumeanalyzer.service.impl;

import com.adarsh.airesumeanalyzer.dto.ResumeUploadResponse;
import com.adarsh.airesumeanalyzer.entity.Resume;
import com.adarsh.airesumeanalyzer.entity.User;
import com.adarsh.airesumeanalyzer.repository.ResumeRepository;
import com.adarsh.airesumeanalyzer.repository.UserRepository;
import com.adarsh.airesumeanalyzer.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementation of {@link ResumeService} for handling resume PDF uploads.
 */
@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final Path uploadDirectory;

    public ResumeServiceImpl(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            @Value("${app.upload.dir:uploads/resumes}") String uploadDir
    ) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
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
}
