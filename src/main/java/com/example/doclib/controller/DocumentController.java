package com.example.doclib.controller;

import com.example.doclib.model.Document;
import com.example.doclib.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * DOCUMENT CONTROLLER - HTTP Layer (REST API)
 * ============================================================
 *
 * This is the CONTROLLER LAYER in our 3-layer architecture.
 * It handles HTTP requests and delegates to the Service layer.
 *
 * @RestController → combines @Controller + @ResponseBody
 *   All methods return JSON automatically (no @ResponseBody needed)
 *
 * @RequestMapping → sets the base URL path for all endpoints
 *   All endpoints in this class start with /api/documents
 *
 * @CrossOrigin → allows the React frontend (on port 3000) to
 *   call this API (on port 8080) without CORS errors.
 *   In production, restrict this to specific domains!
 *
 * Teaching Note - REST Conventions:
 * GET    /documents       → get all (read)
 * GET    /documents/{id}  → get one (read)
 * POST   /documents       → create new
 * PUT    /documents/{id}  → update existing
 * DELETE /documents/{id}  → delete
 * ============================================================
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*") // Allow all origins for demo purposes
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ============================================================
    // GET ALL DOCUMENTS
    // GET /api/documents
    // ============================================================

    /**
     * Returns all documents.
     * CACHING: First call hits MongoDB. Subsequent calls return from cache.
     *
     * Try this:
     * 1. Call GET /api/documents → watch logs → see "CACHE MISS" + MongoDB query
     * 2. Call GET /api/documents again → watch logs → see only "CACHE HIT"
     * 3. Notice the response time difference!
     */
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        log.info("📥 [REQUEST] GET /api/documents - fetching all documents");

        long startTime = System.currentTimeMillis();
        List<Document> documents = documentService.getAllDocuments();
        long duration = System.currentTimeMillis() - startTime;

        // If duration is very fast (<1ms), it was likely a cache hit
        if (duration < 2) {
            log.info("⚡ Response time: {}ms — this was CACHE HIT! (no DB query)", duration);
            documentService.recordCacheHit();
        } else {
            log.info("⏱️  Response time: {}ms — this was CACHE MISS (DB was queried)", duration);
        }

        return ResponseEntity.ok(documents);
    }

    // ============================================================
    // GET DOCUMENT BY ID
    // GET /api/documents/{id}
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable String id) {
        log.info("📥 [REQUEST] GET /api/documents/{} - fetching document by id", id);

        Optional<Document> document = documentService.getDocumentById(id);
        if (document.isEmpty()) {
            log.warn("   ⚠️  Document not found: {}", id);
            return ResponseEntity.notFound().build();
        }

        log.info("   ✅ Document found: '{}'", document.get().getTitle());
        return ResponseEntity.ok(document.get());
    }

    // ============================================================
    // UPLOAD DOCUMENT (with file)
    // POST /api/documents/upload
    // ============================================================

    /**
     * Uploads a document file (e.g., .txt file) and saves metadata to MongoDB.
     *
     * Teaching Note - @RequestParam:
     * The request must include:
     * - title: a form field with the document title
     * - file:  the actual file to upload (MultipartFile)
     *
     * Testing with curl:
     * curl -X POST http://localhost:8080/api/documents/upload \
     *   -F "title=My Document" \
     *   -F "file=@/path/to/file.txt"
     */
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
        @RequestParam("title") String title,
        @RequestParam("file") MultipartFile file) {

        log.info("📥 [REQUEST] POST /api/documents/upload - title='{}', file='{}'",
                 title, file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("   ⚠️  Rejected: empty file");
            return ResponseEntity.badRequest().build();
        }

        try {
            Document saved = documentService.uploadDocument(title, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IOException e) {
            log.error("❌ [ERROR] Failed to upload document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============================================================
    // CREATE DOCUMENT (title + text content, no file)
    // POST /api/documents
    // ============================================================

    /**
     * Creates a document from JSON body (easier for testing from frontend).
     *
     * Request body example:
     * {
     *   "title": "Spring Boot Guide",
     *   "content": "Spring Boot makes it easy to create stand-alone apps..."
     * }
     */
    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.getOrDefault("content", "");

        log.info("📥 [REQUEST] POST /api/documents - title='{}'", title);

        if (title == null || title.isBlank()) {
            log.warn("   ⚠️  Rejected: title is required");
            return ResponseEntity.badRequest().build();
        }

        Document saved = documentService.createDocument(title, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ============================================================
    // SEARCH DOCUMENTS
    // GET /api/documents/search?title=keyword
    // ============================================================

    /**
     * Searches documents by title keyword with caching.
     *
     * Teaching Note:
     * Try searching the same keyword twice and watch the logs.
     * First search: DB query runs, results cached
     * Second search: DB query SKIPPED, results from cache
     *
     * The cache key is the search term itself, so different
     * keywords are cached independently.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String title) {
        log.info("📥 [REQUEST] GET /api/documents/search?title={}", title);

        if (title == null || title.isBlank()) {
            log.warn("   ⚠️  Rejected: title parameter is required");
            return ResponseEntity.badRequest().build();
        }

        long startTime = System.currentTimeMillis();
        List<Document> results = documentService.searchDocuments(title);
        long duration = System.currentTimeMillis() - startTime;

        if (duration < 2) {
            log.info("⚡ Search '{}' served from cache in {}ms!", title, duration);
        } else {
            log.info("⏱️  Search '{}' hit MongoDB in {}ms, {} results found", title, duration, results.size());
        }

        return ResponseEntity.ok(results);
    }

    // ============================================================
    // GENERATE SUMMARY (ASYNC)
    // POST /api/documents/{id}/summary
    // ============================================================

    /**
     * Triggers async summary generation for a document.
     *
     * Teaching Note - Async behavior:
     * This endpoint returns IMMEDIATELY (in ~1-5ms).
     * The actual processing runs in the background for ~5 seconds.
     *
     * After calling this, poll GET /api/documents/{id} and watch
     * the "summaryStatus" field change:
     * "NONE" → "PROCESSING" → "COMPLETED"
     *
     * Compare: without @Async, this would take 5+ seconds to respond!
     */
    @PostMapping("/{id}/summary")
    public ResponseEntity<Map<String, String>> generateSummary(@PathVariable String id) {
        log.info("📥 [REQUEST] POST /api/documents/{}/summary - starting async summary", id);

        Map<String, String> response = documentService.requestSummary(id);

        if (response.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }

        log.info("⚡ Async summary triggered - HTTP response sent in <5ms!");
        return ResponseEntity.accepted().body(response);
    }

    // ============================================================
    // GET STATS
    // GET /api/documents/stats
    // ============================================================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.debug("📥 [REQUEST] GET /api/documents/stats");
        return ResponseEntity.ok(documentService.getStats());
    }
}
