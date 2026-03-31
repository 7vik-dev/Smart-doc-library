package com.example.doclib.service;

import com.example.doclib.model.Document;
import com.example.doclib.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * DOCUMENT SERVICE - Business Logic Layer
 * ============================================================
 *
 * This is the SERVICE LAYER in our 3-layer architecture.
 * It sits between the Controller (HTTP) and Repository (DB).
 *
 * Responsibilities:
 * - Business logic (validation, transformation)
 * - Cache management (@Cacheable, @CacheEvict)
 * - Logging (structured INFO/DEBUG/ERROR messages)
 * - Coordinating between repository and other services
 *
 * Teaching Note - Logging Best Practices:
 * Use structured log messages that answer:
 * WHO did WHAT with WHAT DATA and what was the RESULT?
 *
 * ✅ Good:  "Upload started - title: Spring Boot, file: notes.txt, size: 2048 bytes"
 * ❌ Bad:   "uploading..."
 * ============================================================
 */
@Service
public class DocumentService {

    // SLF4J Logger - all log messages go here
    // In production, these go to log files, Elasticsearch, etc.
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final SummaryService summaryService;

    // Simple in-memory counter for monitoring (resets on restart)
    // In production, use Micrometer metrics instead
    private long totalRequests = 0;
    private long cacheHits = 0;
    private long cacheMisses = 0;

    public DocumentService(DocumentRepository documentRepository, SummaryService summaryService) {
        this.documentRepository = documentRepository;
        this.summaryService = summaryService;
    }

    // ============================================================
    // GET ALL DOCUMENTS - WITH CACHING
    // ============================================================

    /**
     * Returns a paginated list of documents.
     */
    @Cacheable("documents")
    public Page<Document> getAllDocuments(int page, int size) {
        log.info("   🗄️  PostgreSQL query: findAll(page={}, size={})", page, size);
        recordCacheMiss();
        return documentRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // ============================================================
    // UPLOAD DOCUMENT
    // ============================================================

    /**
     * Uploads a document - extracts content and saves to PostgreSQL.
     * Also clears the cache so the next getAllDocuments() is fresh.
     *
     * Teaching Note - @CacheEvict:
     * When we save a new document, the cached list is stale (outdated).
     * @CacheEvict removes the old cache entry, forcing the next
     * getAllDocuments() to re-query PostgreSQL and get fresh results.
     */
    @CacheEvict(value = {"documents", "search"}, allEntries = true)
    public Document uploadDocument(String title, MultipartFile file) throws IOException {
        log.info("📤 [UPLOAD START] title='{}', file='{}', size={} bytes",
                 title, file.getOriginalFilename(), file.getSize());

        long startTime = System.currentTimeMillis();

        // Extract text content from the file
        String content = new String(file.getBytes());

        // Build and save the document
        Document document = Document.builder()
            .title(title)
            .content(content)
            .fileName(file.getOriginalFilename())
            .createdAt(LocalDateTime.now())
            .status("PENDING")
            .build();

        Document saved = documentRepository.save(document);
        long duration = System.currentTimeMillis() - startTime;

        log.info("✅ [UPLOAD COMPLETE] Document saved - id='{}', duration={}ms", saved.getId(), duration);
        log.info("   🗑️  Cache 'documents' and 'search' cleared (stale data evicted)");

        return saved;
    }

    /**
     * Alternative upload without a real file (just title + content text).
     * Useful for quick testing from the frontend form.
     */
    @CacheEvict(value = {"documents", "search"}, allEntries = true)
    public Document createDocument(String title, String content) {
        log.info("📝 [CREATE START] Creating document with title='{}'", title);

        Document document = Document.builder()
            .title(title)
            .content(content)
            .fileName("manual-entry.txt")
            .createdAt(LocalDateTime.now())
            .status("PENDING")
            .build();

        Document saved = documentRepository.save(document);
        log.info("✅ [CREATE COMPLETE] Document created - id='{}'", saved.getId());
        log.info("   🗑️  Cache cleared - next list request hits PostgreSQL");

        return saved;
    }

    // ============================================================
    // SEARCH DOCUMENTS - WITH CACHING
    // ============================================================

    /**
     * Searches documents by title keyword with caching.
     *
     * Teaching Note - @Cacheable with key:
     * The 'key' parameter tells Spring what to use as the cache key.
     * "#title" means use the 'title' method parameter as the key.
     *
     * So searching "spring" and "mongodb" are cached separately:
     * cache["spring"]  → list of documents with "spring" in title
     * cache["mongodb"] → list of documents with "mongodb" in title
     *
     * SpEL (Spring Expression Language) is used for the key:
     * #title         → the parameter named 'title'
     * #title.toUpperCase() → all caps version
     * "#title + '_' + #page" → compound key with two params
     */
    @Cacheable(value = "search", key = "#title")
    public List<Document> searchDocuments(String title) {
        totalRequests++;
        log.info("🔍 [SEARCH CACHE MISS] Searching PostgreSQL for title containing: '{}'", title);
        cacheMisses++;

        long startTime = System.currentTimeMillis();
        List<Document> results = documentRepository.findByTitleContainingIgnoreCase(title);
        long duration = System.currentTimeMillis() - startTime;

        log.info("   Found {} results for '{}' in {}ms - now cached", results.size(), title, duration);
        return results;
    }

    // ============================================================
    // GET DOCUMENT BY ID
    // ============================================================

    public Optional<Document> getDocumentById(String id) {
        log.debug("🔎 Looking up document by id: {}", id);
        return documentRepository.findById(id);
    }

    // ============================================================
    // REQUEST SUMMARY (triggers async processing)
    // ============================================================

    /**
     * Starts async summary generation and returns immediately.
     *
     * Teaching Note - The async flow:
     * 1. Client calls POST /documents/{id}/summary
     * 2. This method runs on the HTTP request thread
     * 3. summaryService.generateSummaryAsync() is called
     *    → The task is handed to the thread pool
     *    → The HTTP thread is immediately free
     * 4. HTTP response is sent: {"status": "PROCESSING"}  (in ~1ms!)
     * 5. Background thread processes for 5 seconds
     * 6. Background thread updates MongoDB
     * 7. Client can poll GET /documents/{id} to check status
     *
     * Without @Async: client would wait 5+ seconds for the response!
     * With @Async: client gets a response in ~1ms!
     */
    public Map<String, String> requestSummary(String documentId) {
        log.info("📋 [SUMMARY REQUEST] Received summary request for document: {}", documentId);

        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            log.warn("   ⚠️  Document not found: {}", documentId);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Document not found: " + documentId);
            return error;
        }

        Document doc = docOpt.get();

        // If already processing or completed, don't start again
        if ("PROCESSING".equals(doc.getStatus())) {
            log.info("   ℹ️  Summary already being processed for: {}", documentId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "PROCESSING");
            response.put("message", "Summary is already being generated");
            response.put("documentId", documentId);
            return response;
        }

        // Hand task to async thread pool - this returns IMMEDIATELY
        log.info("   🚀 Handing task to async thread pool...");
        summaryService.generateSummaryAsync(documentId);

        log.info("   ⚡ HTTP response sent immediately (background task is running)");

        Map<String, String> response = new HashMap<>();
        response.put("status", "PROCESSING");
        response.put("message", "Summary generation started. Check back in a few seconds.");
        response.put("documentId", documentId);
        return response;
    }

    // ============================================================
    // MONITORING STATISTICS
    // ============================================================

    /**
     * Returns custom metrics for the monitoring dashboard.
     * In production, use Micrometer + Actuator for this.
     */
    public Map<String, Object> getStats() {
        long totalDocs = documentRepository.count();
        long completedSummaries = documentRepository.countByStatus("COMPLETED");
        long processingSummaries = documentRepository.countByStatus("PROCESSING");
        long pendingSummaries = documentRepository.countByStatus("PENDING");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", totalDocs);
        stats.put("completedSummaries", completedSummaries);
        stats.put("processingSummaries", processingSummaries);
        stats.put("pendingSummaries", pendingSummaries);
        stats.put("totalApiRequests", totalRequests);
        stats.put("cacheHits", cacheHits);
        stats.put("cacheMisses", cacheMisses);
        stats.put("cacheHitRatio", totalRequests > 0
            ? String.format("%.1f%%", (double) cacheHits / totalRequests * 100)
            : "N/A");

        return stats;
    }

    /**
     * Called by the controller when a cached response is served.
     * Increments the hit counter for monitoring.
     */
    public void recordCacheHit() {
        cacheHits++;
        totalRequests++;
        log.info("⚡ [CACHE HIT] Response served from memory (no DB query!) - hit #{}", cacheHits);
    }

    /**
     * Increments the miss counter for monitoring.
     */
    private void recordCacheMiss() {
        cacheMisses++;
        totalRequests++;
    }
}
