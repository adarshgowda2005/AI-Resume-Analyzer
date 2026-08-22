package com.adarsh.airesumeanalyzer.service;

/**
 * Service interface for extracting plain text content from PDF resume files.
 */
public interface PdfTextExtractionService {

    /**
     * Extracts text content from a PDF file located at the specified file path.
     *
     * @param filePath the path to the PDF file on disk
     * @return extracted plain text content from all pages of the PDF
     * @throws com.adarsh.airesumeanalyzer.exception.PdfParsingException if the file is missing, invalid, or unreadable
     */
    String extractText(String filePath);
}
