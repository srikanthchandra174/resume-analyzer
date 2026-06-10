package com.srikanth.ai.resumeanalyzer.service;

import com.srikanth.ai.resumeanalyzer.exception.PdfExtractionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PdfExtractionService
 */
@DisplayName("PdfExtractionService Tests")
class PdfExtractionServiceTest {

    private PdfExtractionService pdfExtractionService;

    @BeforeEach
    void setUp() {
        pdfExtractionService = new PdfExtractionService();
    }

    @Test
    @DisplayName("extractTextFromPdf should throw exception for null file")
    void testExtractTextNullFile() {
        assertThrows(PdfExtractionException.class, () -> {
            pdfExtractionService.extractTextFromPdf(null);
        });
    }

    @Test
    @DisplayName("extractTextFromPdf should throw exception for empty file")
    void testExtractTextEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );

        assertThrows(PdfExtractionException.class, () -> {
            pdfExtractionService.extractTextFromPdf(emptyFile);
        });
    }

    @Test
    @DisplayName("extractTextFromPdf should throw exception for file over 10MB")
    void testExtractTextFileTooLarge() {
        // Mock a file with size > 10MB but don't allocate actual memory
        MultipartFile largeFile = mock(MultipartFile.class);
        when(largeFile.isEmpty()).thenReturn(false);
        when(largeFile.getSize()).thenReturn(11 * 1024 * 1024L); // 11 MB

        assertThrows(PdfExtractionException.class, () -> {
            pdfExtractionService.extractTextFromPdf(largeFile);
        });
    }

    @Test
    @DisplayName("extractTextFromPdf should throw exception for wrong content type")
    void testExtractTextWrongContentType() {
        MockMultipartFile wrongType = new MockMultipartFile(
            "file",
            "document.pdf",
            "text/plain",
            "some content".getBytes()
        );

        assertThrows(PdfExtractionException.class, () -> {
            pdfExtractionService.extractTextFromPdf(wrongType);
        });
    }

    @Test
    @DisplayName("extractTextFromPdf should throw exception for wrong extension")
    void testExtractTextWrongExtension() {
        MockMultipartFile wrongExtension = new MockMultipartFile(
            "file",
            "document.txt",
            "application/pdf",
            "some content".getBytes()
        );

        assertThrows(PdfExtractionException.class, () -> {
            pdfExtractionService.extractTextFromPdf(wrongExtension);
        });
    }

    @Test
    @DisplayName("extractTextFromPdf should throw exception for corrupt PDF")
    void testExtractTextCorruptPdf() {
        byte[] corruptPdfBytes = "This is not a PDF file at all".getBytes();
        MockMultipartFile corruptFile = new MockMultipartFile(
            "file",
            "corrupt.pdf",
            "application/pdf",
            corruptPdfBytes
        );

        assertThrows(PdfExtractionException.class, () -> {
            pdfExtractionService.extractTextFromPdf(corruptFile);
        });
    }

    @Test
    @DisplayName("extractTextFromPdf should extract text from valid PDF")
    void testExtractTextValidPdf() throws Exception {
        String expectedText = "Hello World from PDF";
        byte[] pdfBytes = createSamplePdf(expectedText);

        MockMultipartFile pdfFile = new MockMultipartFile(
            "file",
            "sample.pdf",
            "application/pdf",
            pdfBytes
        );

        String extractedText = pdfExtractionService.extractTextFromPdf(pdfFile);

        assertNotNull(extractedText);
        assertTrue(extractedText.contains(expectedText),
            "Extracted text should contain: " + expectedText);
    }

    /**
     * Creates a sample PDF in memory with the given text
     * Uses PDFBox 3.x API with PDType1Font
     */
    private byte[] createSamplePdf(String text) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(text);
                contentStream.endText();
            }

            document.save(out);
        }

        return out.toByteArray();
    }
}

