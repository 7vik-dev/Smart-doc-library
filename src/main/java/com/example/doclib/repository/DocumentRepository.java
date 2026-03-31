package com.example.doclib.repository;

import com.example.doclib.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ============================================================
 * DOCUMENT REPOSITORY - Data Access Layer (PostgreSQL)
 * ============================================================
 *
 * This interface is the DATABASE LAYER of the application.
 * Spring Data JPA auto-implements all basic CRUD methods.
 *
 * Teaching Note - What JpaRepository gives us for FREE:
 * ✅ save(document)         → INSERT or UPDATE
 * ✅ findById(id)           → SELECT by primary key
 * ✅ findAll()              → SELECT all records
 * ✅ deleteById(id)         → DELETE by id
 * ✅ count()                → COUNT all records
 * ✅ existsById(id)         → check existence
 *
 * We can also declare custom queries using method naming conventions.
 * Spring Data parses the method name and generates the SQL query!
 *
 * Example:
 *   findByTitle("Spring Boot") → SELECT * FROM documents WHERE title = 'Spring Boot'
 * ============================================================
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    /**
     * Custom finder: finds documents where title contains the given string.
     * Spring Data auto-generates: SELECT * FROM documents WHERE LOWER(title) LIKE %keyword%
     *
     * @param title - keyword to search in document titles
     * @return list of matching documents
     */
    List<Document> findByTitleContainingIgnoreCase(String title);

    /**
     * Count documents by their summary generation status.
     *
     * @param status - the status value (e.g., "COMPLETED")
     * @return count of documents with that status
     */
    long countByStatus(String status);
}
