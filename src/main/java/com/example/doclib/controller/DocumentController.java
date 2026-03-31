package com.example.doclib.controller;

import com.example.doclib.model.Document;
import com.example.doclib.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("📥 [REQUEST] GET /api/documents - page={}, size={}", page, size);
        long startTime = System.currentTimeMillis();
        Page<Document> documents = documentService.getAllDocuments(page, size);
        long duration = System.currentTimeMillis() - startTime;

        if (duration < 2) {
            log.info("⚡ Response time: {}ms — this was CACHE HIT!", duration);
            documentService.recordCacheHit();
        } else {
            log.info("⏱️  Response time: {}ms — this was CACHE MISS", duration);
        }
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable String id) {
        log.info("📥 [REQUEST] GET /api/documents/{} - fetching document by id", id);
        Optional<Document> document = documentService.getDocumentById(id);
        return document.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) {
        log.info("📥 [REQUEST] POST /api/documents/upload - title='{}', file='{}'", title, file.getOriginalFilename());
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        try {
            Document saved = documentService.uploadDocument(title, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IOException e) {
            log.error("❌ Failed to upload document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Document> createDocument(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.getOrDefault("content", "");
        log.info("📥 [REQUEST] POST /api/documents - title='{}'", title);
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().build();
        Document saved = documentService.createDocument(title, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDocuments(@RequestParam String title) {
        log.info("📥 [REQUEST] GET /api/documents/search?title={}", title);
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().build();
        long startTime = System.currentTimeMillis();
        List<Document> results = documentService.searchDocuments(title);
        long duration = System.currentTimeMillis() - startTime;
        if (duration < 2) log.info("⚡ Search '{}' served from cache in {}ms!", title, duration);
        else log.info("⏱️  Search '{}' hit MongoDB in {}ms", title, duration);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{id}/summary")
    public ResponseEntity<Map<String, String>> generateSummary(@PathVariable String id) {
        log.info("📥 [REQUEST] POST /api/documents/{}/summary - starting async summary", id);
        Map<String, String> response = documentService.requestSummary(id);
        if (response.containsKey("error")) return ResponseEntity.notFound().build();
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(documentService.getStats());
    }
}
