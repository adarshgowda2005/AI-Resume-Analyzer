package com.adarsh.airesumeanalyzer.service;

import com.adarsh.airesumeanalyzer.exception.PdfParsingException;
import com.adarsh.airesumeanalyzer.service.impl.PdfTextExtractionServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextExtractionServiceTest {

    private PdfTextExtractionService pdfTextExtractionService;

    @BeforeEach
    void setUp() {
        pdfTextExtractionService = new PdfTextExtractionServiceImpl();
    }

    @Test
    void extractText_WhenValidPdf_ShouldExtractTextSuccessfully(@TempDir Path tempDir) throws IOException {
        // Given - Create a valid PDF document in memory and write to temp file
        Path pdfPath = tempDir.resolve("sample_resume.pdf");
        String sampleText = "John Doe - Senior Software Engineer";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(sampleText);
                contentStream.endText();
            }

            document.save(pdfPath.toFile());
        }

        // When
        String extractedText = pdfTextExtractionService.extractText(pdfPath.toString());

        // Then
        assertNotNull(extractedText);
        assertTrue(extractedText.contains("John Doe"));
        assertTrue(extractedText.contains("Senior Software Engineer"));
    }

    @Test
    void extractText_WhenFileDoesNotExist_ShouldThrowPdfParsingException(@TempDir Path tempDir) {
        // Given
        Path missingPath = tempDir.resolve("non_existent_resume.pdf");

        // When & Then
        PdfParsingException exception = assertThrows(PdfParsingException.class, () ->
                pdfTextExtractionService.extractText(missingPath.toString()));

        assertTrue(exception.getMessage().contains("PDF file not found"));
    }

    @Test
    void extractText_WhenFileIsInvalidOrCorrupt_ShouldThrowPdfParsingException(@TempDir Path tempDir) throws IOException {
        // Given - Create a non-PDF file saved with .pdf extension
        Path corruptFile = tempDir.resolve("corrupt_file.pdf");
        Files.writeString(corruptFile, "This is not a valid PDF file structure.");

        // When & Then
        PdfParsingException exception = assertThrows(PdfParsingException.class, () ->
                pdfTextExtractionService.extractText(corruptFile.toString()));

        assertTrue(exception.getMessage().contains("Failed to extract text from PDF document")
                || exception.getMessage().contains("Unexpected error"));
    }

    @Test
    void extractText_WhenPathIsDirectory_ShouldThrowPdfParsingException(@TempDir Path tempDir) {
        // Given - Pass a directory path instead of a regular file
        // When & Then
        PdfParsingException exception = assertThrows(PdfParsingException.class, () ->
                pdfTextExtractionService.extractText(tempDir.toString()));

        assertTrue(exception.getMessage().contains("not a valid file"));
    }

    @Test
    void extractText_WhenPathIsNullorEmpty_ShouldThrowPdfParsingException() {
        // When & Then
        assertThrows(PdfParsingException.class, () -> pdfTextExtractionService.extractText(null));
        assertThrows(PdfParsingException.class, () -> pdfTextExtractionService.extractText("   "));
    }
}
