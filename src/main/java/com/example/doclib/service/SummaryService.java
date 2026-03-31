package com.example.doclib.service;

import com.example.doclib.model.Document;
import com.example.doclib.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ============================================================
 * SUMMARY SERVICE - Async Processing Demo
 * ============================================================
 *
 * Teaching Note - What is @Async?
 *
 * When you call a regular method:
 *   Thread A calls method → Thread A waits → method finishes → Thread A continues
 *
 * When you call an @Async method:
 *   Thread A calls method → Task handed to thread pool → Thread A immediately continues
 *   Meanwhile, Thread B (from pool) processes the method in background
 *
 * IMPORTANT RULE: @Async only works when the method is called
 * from a DIFFERENT class (Spring uses proxy pattern).
 * That's why SummaryService is separate from DocumentService!
 *
 * Real-world use cases for @Async:
 * - Sending emails after signup (don't make user wait)
 * - Generating reports or exports
 * - Calling slow external APIs
 * - Processing uploaded files
 * - AI summarization (exactly what we demo here)
 * ============================================================
 */
@Service
public class SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryService.class);

    private final DocumentRepository documentRepository;

    public SummaryService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Generates a document summary asynchronously.
     *
     * Teaching Note:
     * @Async makes this method run on a background thread (DocLib-Async-N).
     * The caller gets control back immediately without waiting.
     *
     * The method simulates a slow AI processing step using Thread.sleep(5000).
     * In a real app, this would call an AI API like OpenAI or Google Gemini.
     *
     * Watch the logs when you call this:
     * 1. DocumentController logs "Summary requested"
     * 2. HTTP response is sent immediately (fast!)
     * 3. A few seconds later, SummaryService logs "Processing complete"
     *    on a DIFFERENT thread (DocLib-Async-1)
     *
     * @param documentId - the document to summarize
     */
    @Async("taskExecutor")
    public void generateSummaryAsync(String documentId) {
        log.info("🚀 [ASYNC START] Processing summary for: {}", documentId);

        // Update status to PROCESSING immediately
        updateSummaryStatus(documentId, "PROCESSING", null);

        try {
            // Simulation of complex AI/NLP processing
            // In a real app, this would call an OpenAI/Azure/Local LLM API
            Thread.sleep(5000); // 5 seconds of work

            // Generate a simple mock summary based on document content
            Optional<Document> docOpt = documentRepository.findById(documentId);
            if (docOpt.isEmpty()) {
                log.warn("   ⚠️  Document not found during async processing: {}", documentId);
                return;
            }

            Document doc = docOpt.get();
            String content = doc.getContent() != null ? doc.getContent() : "";

            // Simple summary: take first 150 chars + word count
            String summary = buildSummary(doc.getTitle(), content);

            // Save the completed summary back to PostgreSQL
            updateSummaryStatus(documentId, "COMPLETED", summary);

            log.info("✅ [ASYNC COMPLETE] Summary ready for document: {}", documentId);
            log.info("   Summary: {}", summary.substring(0, Math.min(summary.length(), 60)) + "...");

        } catch (InterruptedException e) {
            log.error("❌ [ASYNC ERROR] Summary generation interrupted for: {}", documentId, e);
            updateSummaryStatus(documentId, "ERROR", "Processing was interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("❌ [ASYNC ERROR] Summary generation failed for: {}", documentId, e);
            updateSummaryStatus(documentId, "ERROR", "Processing failed: " + e.getMessage());
        }
    }

    /**
     * Helper: updates the document's summary fields in PostgreSQL
     */
    private void updateSummaryStatus(String documentId, String status, String summary) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setStatus(status);
            if (summary != null) {
                doc.setSummary(summary);
            }
            documentRepository.save(doc);
            log.debug("   📝 Database updated - status: {} for doc: {}", status, documentId);
        });
    }

    /**
     * Helper: builds a simple mock summary from the document
     */
    private String buildSummary(String title, String content) {
        int wordCount = content.isEmpty() ? 0 : content.trim().split("\\s+").length;
        String preview = content.length() > 200
            ? content.substring(0, 200) + "..."
            : content;

        return String.format(
            "Summary of '%s': This document contains %d words. Preview: %s",
            title, wordCount, preview.isEmpty() ? "[No content provided]" : preview
        );
    }
}
