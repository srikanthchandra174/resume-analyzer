package com.srikanth.ai.resumeanalyzer.exception;

/**
 * Exception thrown when AI analysis fails
 */
public class AnalysisException extends RuntimeException {

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}

