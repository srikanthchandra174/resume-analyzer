package com.srikanth.ai.resumeanalyzer.service;

import com.srikanth.ai.resumeanalyzer.exception.PdfExtractionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * Service for extracting text from PDF files
 */
@Slf4j
@Service
public class PdfExtractionService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String PDF_MIME_TYPE = "application/pdf";

    /**
     * Extracts text from a PDF file
     *
     * @param file The MultipartFile containing the PDF
     * @return The extracted text from the PDF
     * @throws PdfExtractionException if extraction fails
     */
    public String extractTextFromPdf(MultipartFile file) {
        validateFile(file);

        try {
            log.info("Starting PDF text extraction for file: {}", file.getOriginalFilename());

            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                if (document.isEncrypted()) {
                    log.warn("PDF is encrypted, attempting to open with empty password");
                    document.setAllSecurityToBeRemoved(true);
                }

                PDFTextStripper textStripper = new PDFTextStripper();
                String extractedText = textStripper.getText(document);

                log.info("Successfully extracted {} characters from PDF", extractedText.length());
                return extractedText.trim();
            }
        } catch (IOException e) {
            log.error("Error extracting text from PDF: {}", file.getOriginalFilename(), e);
            throw new PdfExtractionException(
                String.format("Failed to extract text from PDF '%s': %s",
                    file.getOriginalFilename(), e.getMessage()),
                e
            );
        }
    }

    /**
     * Validates the uploaded file
     *
     * @param file The file to validate
     * @throws PdfExtractionException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PdfExtractionException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PdfExtractionException(
                String.format("File size exceeds maximum allowed size of %dMB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(PDF_MIME_TYPE)) {
            throw new PdfExtractionException("File must be a valid PDF document");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new PdfExtractionException("File must have .pdf extension");
        }
    }
}

