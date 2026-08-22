package com.adarsh.airesumeanalyzer.exception;

/**
 * Custom runtime exception thrown when a PDF file cannot be read or parsed.
 */
public class PdfParsingException extends RuntimeException {

    public PdfParsingException(String message) {
        super(message);
    }

    public PdfParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
