package com.adarsh.airesumeanalyzer.service.impl;

import com.adarsh.airesumeanalyzer.exception.PdfParsingException;
import com.adarsh.airesumeanalyzer.service.PdfTextExtractionService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of {@link PdfTextExtractionService} using Apache PDFBox.
 */
@Service
public class PdfTextExtractionServiceImpl implements PdfTextExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(PdfTextExtractionServiceImpl.class);

    @Override
    public String extractText(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new PdfParsingException("File path must not be null or empty.");
        }

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            logger.error("PDF file not found at path: {}", filePath);
            throw new PdfParsingException("PDF file not found at path: " + filePath);
        }

        if (!Files.isRegularFile(path)) {
            logger.error("Specified path is not a regular file: {}", filePath);
            throw new PdfParsingException("Specified path is not a valid file: " + filePath);
        }

        File pdfFile = path.toFile();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int pageCount = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            int charCount = extractedText != null ? extractedText.length() : 0;
            logger.info("Successfully extracted text from PDF file [pages: {}, characters: {}] at path: {}",
                    pageCount, charCount, filePath);

            return extractedText != null ? extractedText : "";
        } catch (IOException e) {
            logger.error("Failed to parse PDF document at path: {}", filePath, e);
            throw new PdfParsingException("Failed to extract text from PDF document: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while parsing PDF at path: {}", filePath, e);
            throw new PdfParsingException("Unexpected error during PDF parsing: " + e.getMessage(), e);
        }
    }
}
