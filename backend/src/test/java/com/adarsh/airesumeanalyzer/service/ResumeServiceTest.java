package com.adarsh.airesumeanalyzer.service;

import com.adarsh.airesumeanalyzer.dto.ResumeResponse;
import com.adarsh.airesumeanalyzer.entity.Resume;
import com.adarsh.airesumeanalyzer.entity.Role;
import com.adarsh.airesumeanalyzer.entity.User;
import com.adarsh.airesumeanalyzer.repository.ResumeRepository;
import com.adarsh.airesumeanalyzer.repository.UserRepository;
import com.adarsh.airesumeanalyzer.service.impl.ResumeServiceImpl;
import com.adarsh.airesumeanalyzer.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.adarsh.airesumeanalyzer.dto.ResumeParsedResponse;
import com.adarsh.airesumeanalyzer.service.PdfTextExtractionService;
import com.adarsh.airesumeanalyzer.service.ResumeParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PdfTextExtractionService pdfTextExtractionService;

    @Mock
    private ResumeParserService resumeParserService;

    private ResumeServiceImpl resumeService;

    private User testUser;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeServiceImpl(
                resumeRepository,
                userRepository,
                pdfTextExtractionService,
                resumeParserService,
                "uploads/resumes"
        );
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .password("secretHash")
                .role(Role.USER)
                .build();
    }

    @Test
    void getUserResumes_WhenResumesExist_ShouldReturnResumeResponses() {
        // Given
        Resume resume1 = Resume.builder()
                .id(10L)
                .originalFileName("CV_John.pdf")
                .storedFileName("uuid-1.pdf")
                .filePath("/uploads/uuid-1.pdf")
                .uploadedAt(LocalDateTime.now())
                .user(testUser)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByUserOrderByUploadedAtDesc(testUser)).thenReturn(List.of(resume1));

        // When
        List<ResumeResponse> responses = resumeService.getUserResumes("john@example.com");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        ResumeResponse res = responses.get(0);
        assertEquals(10L, res.getId());
        assertEquals("CV_John.pdf", res.getOriginalFileName());
        assertEquals("uuid-1.pdf", res.getStoredFileName());
        assertEquals("John Doe", res.getUploadedByName());
        assertEquals("john@example.com", res.getUploadedByEmail());

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(resumeRepository, times(1)).findByUserOrderByUploadedAtDesc(testUser);
    }

    @Test
    void getUserResumes_WhenNoResumesExist_ShouldReturnEmptyListCleanly() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByUserOrderByUploadedAtDesc(testUser)).thenReturn(Collections.emptyList());

        // When
        List<ResumeResponse> responses = resumeService.getUserResumes("john@example.com");

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(resumeRepository, times(1)).findByUserOrderByUploadedAtDesc(testUser);
    }

    @Test
    void getUserResumes_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                resumeService.getUserResumes("nonexistent@example.com"));

        assertTrue(exception.getMessage().contains("Authenticated user not found"));
    }

    @Test
    void getResumeById_WhenExistsAndOwnedByUser_ShouldReturnResumeResponse() {
        // Given
        Long resumeId = 5L;
        Resume resume = Resume.builder()
                .id(resumeId)
                .originalFileName("Software_Developer_Resume.pdf")
                .storedFileName("uuid-5.pdf")
                .filePath("/uploads/uuid-5.pdf")
                .uploadedAt(LocalDateTime.now())
                .user(testUser)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByIdAndUser(resumeId, testUser)).thenReturn(Optional.of(resume));

        // When
        ResumeResponse response = resumeService.getResumeById(resumeId, "john@example.com");

        // Then
        assertNotNull(response);
        assertEquals(resumeId, response.getId());
        assertEquals("Software_Developer_Resume.pdf", response.getOriginalFileName());
        assertEquals("uuid-5.pdf", response.getStoredFileName());
        assertEquals("John Doe", response.getUploadedByName());
        assertEquals("john@example.com", response.getUploadedByEmail());

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(resumeRepository, times(1)).findByIdAndUser(resumeId, testUser);
    }

    @Test
    void getResumeById_WhenNonExistent_ShouldThrowResourceNotFoundException() {
        // Given
        Long resumeId = 99L;
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByIdAndUser(resumeId, testUser)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                resumeService.getResumeById(resumeId, "john@example.com"));

        assertEquals("Resume not found with id: 99", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(resumeRepository, times(1)).findByIdAndUser(resumeId, testUser);
    }

    @Test
    void getResumeById_WhenBelongsToAnotherUser_ShouldThrowResourceNotFoundException() {
        // Given - User B requests User A's resume ID (1L)
        User userB = User.builder()
                .id(2L)
                .fullName("Jane Smith")
                .email("jane@example.com")
                .password("hashB")
                .role(Role.USER)
                .build();

        Long userAResumeId = 1L;

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(userB));
        when(resumeRepository.findByIdAndUser(userAResumeId, userB)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                resumeService.getResumeById(userAResumeId, "jane@example.com"));

        assertEquals("Resume not found with id: 1", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("jane@example.com");
        verify(resumeRepository, times(1)).findByIdAndUser(userAResumeId, userB);
    }

    @Test
    void deleteResume_WhenExistsAndOwnedByUser_ShouldDeletePhysicalFileAndDatabaseRecord(@TempDir Path tempUploadDir) throws IOException {
        // Given
        ResumeServiceImpl serviceWithTempDir = new ResumeServiceImpl(
                resumeRepository,
                userRepository,
                pdfTextExtractionService,
                resumeParserService,
                tempUploadDir.toString()
        );
        String storedFileName = "test-resume.pdf";
        Path physicalFile = tempUploadDir.resolve(storedFileName);
        Files.writeString(physicalFile, "%PDF-1.4 test content");
        assertTrue(Files.exists(physicalFile));

        Long resumeId = 1L;
        Resume resume = Resume.builder()
                .id(resumeId)
                .originalFileName("My_Resume.pdf")
                .storedFileName(storedFileName)
                .filePath(physicalFile.toString())
                .uploadedAt(LocalDateTime.now())
                .user(testUser)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByIdAndUser(resumeId, testUser)).thenReturn(Optional.of(resume));

        // When
        serviceWithTempDir.deleteResume(resumeId, "john@example.com");

        // Then
        assertFalse(Files.exists(physicalFile), "Physical file should have been deleted from disk");
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(resumeRepository, times(1)).findByIdAndUser(resumeId, testUser);
        verify(resumeRepository, times(1)).delete(resume);
    }

    @Test
    void deleteResume_WhenNonExistent_ShouldThrowResourceNotFoundException() {
        // Given
        Long resumeId = 999L;
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByIdAndUser(resumeId, testUser)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                resumeService.deleteResume(resumeId, "john@example.com"));

        assertEquals("Resume not found with id: 999", exception.getMessage());
        verify(resumeRepository, never()).delete(any());
    }

    @Test
    void deleteResume_WhenBelongsToAnotherUser_ShouldThrowResourceNotFoundExceptionAndNotDelete() {
        // Given
        User userB = User.builder()
                .id(2L)
                .fullName("User B")
                .email("userb@example.com")
                .password("hashB")
                .role(Role.USER)
                .build();

        Long userAResumeId = 10L;

        when(userRepository.findByEmail("userb@example.com")).thenReturn(Optional.of(userB));
        when(resumeRepository.findByIdAndUser(userAResumeId, userB)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                resumeService.deleteResume(userAResumeId, "userb@example.com"));

        assertEquals("Resume not found with id: 10", exception.getMessage());
        verify(resumeRepository, never()).delete(any());
    }

    @Test
    void deleteResume_WhenPhysicalFileMissing_ShouldStillDeleteDatabaseRecord(@TempDir Path tempUploadDir) {
        // Given - physical file does not exist in temp directory
        ResumeServiceImpl serviceWithTempDir = new ResumeServiceImpl(
                resumeRepository,
                userRepository,
                pdfTextExtractionService,
                resumeParserService,
                tempUploadDir.toString()
        );
        String storedFileName = "missing-file.pdf";
        Path missingFile = tempUploadDir.resolve(storedFileName);
        assertFalse(Files.exists(missingFile));

        Long resumeId = 2L;
        Resume resume = Resume.builder()
                .id(resumeId)
                .originalFileName("Missing.pdf")
                .storedFileName(storedFileName)
                .filePath(missingFile.toString())
                .uploadedAt(LocalDateTime.now())
                .user(testUser)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByIdAndUser(resumeId, testUser)).thenReturn(Optional.of(resume));

        // When
        serviceWithTempDir.deleteResume(resumeId, "john@example.com");

        // Then - DB delete is still executed without failure
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(resumeRepository, times(1)).findByIdAndUser(resumeId, testUser);
        verify(resumeRepository, times(1)).delete(resume);
    }

    @Test
    void parseResume_WhenOwnedByUser_ShouldExtractTextAndReturnParsedResponse() {
        // Given
        Long resumeId = 1L;
        Resume resume = Resume.builder()
                .id(resumeId)
                .originalFileName("John_Doe_Resume.pdf")
                .storedFileName("uuid-10.pdf")
                .filePath("/uploads/uuid-10.pdf")
                .uploadedAt(LocalDateTime.now())
                .user(testUser)
                .build();

        String rawText = "John Doe\njohn@example.com\nSUMMARY\nSoftware Developer\nSKILLS\nJava Spring";
        ResumeParsedResponse expectedResponse = ResumeParsedResponse.builder()
                .name("John Doe")
                .email("john@example.com")
                .skills(List.of("Java", "Spring"))
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findByIdAndUser(resumeId, testUser)).thenReturn(Optional.of(resume));
        when(pdfTextExtractionService.extractText("/uploads/uuid-10.pdf")).thenReturn(rawText);
        when(resumeParserService.parseResume(rawText)).thenReturn(expectedResponse);

        // When
        ResumeParsedResponse result = resumeService.parseResume(resumeId, "john@example.com");

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(List.of("Java", "Spring"), result.getSkills());

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(resumeRepository, times(1)).findByIdAndUser(resumeId, testUser);
        verify(pdfTextExtractionService, times(1)).extractText("/uploads/uuid-10.pdf");
        verify(resumeParserService, times(1)).parseResume(rawText);
    }
}
