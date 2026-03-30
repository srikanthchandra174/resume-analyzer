package com.srikanth.ai.resumeanalyzer.exception;

/**
 * Exception thrown when PDF extraction fails
 */
public class PdfExtractionException extends RuntimeException {

    public PdfExtractionException(String message) {
        super(message);
    }

    public PdfExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}

