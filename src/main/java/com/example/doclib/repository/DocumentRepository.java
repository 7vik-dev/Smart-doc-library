package com.example.doclib.repository;

import com.example.doclib.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ============================================================
 * DOCUMENT REPOSITORY - Data Access Layer
 * ============================================================
 *
 * This interface is the DATABASE LAYER of the application.
 * Spring Data MongoDB auto-implements all basic CRUD methods.
 *
 * Teaching Note - What MongoRepository gives us for FREE:
 * ✅ save(document)         → INSERT or UPDATE
 * ✅ findById(id)           → SELECT by primary key
 * ✅ findAll()              → SELECT all documents
 * ✅ deleteById(id)         → DELETE by id
 * ✅ count()                → COUNT all documents
 * ✅ existsById(id)         → check existence
 *
 * We can also declare custom queries using method naming conventions.
 * Spring Data parses the method name and generates the MongoDB query!
 *
 * Example:
 *   findByTitle("Spring Boot") → db.documents.find({title: "Spring Boot"})
 *   findByTitleContaining("Boot") → uses regex match
 * ============================================================
 */
@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {

    /**
     * Custom finder: finds documents where title contains the given string.
     * Spring Data auto-generates: db.documents.find({title: /keyword/i})
     *
     * @param title - keyword to search in document titles
     * @return list of matching documents
     */
    List<Document> findByTitleContainingIgnoreCase(String title);

    /**
     * Count documents that have a completed summary.
     * Used for monitoring/statistics.
     *
     * @param status - the summaryStatus value (e.g., "COMPLETED")
     * @return count of documents with that status
     */
    long countBySummaryStatus(String status);
}
