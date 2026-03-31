package com.example.doclib.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * ============================================================
 * DOCUMENT MODEL - PostgreSQL Table
 * ============================================================
 *
 * This class maps to a SQL table called "documents".
 *
 * Teaching Note:
 * @Entity    → marks this class as a JPA entity (SQL table)
 * @Table     → specifies the table name in the database
 * @Id        → marks the primary key field
 * @GeneratedValue → handles auto-incrementing the ID
 * @Column    → (optional) configures column details like length or indexing
 * ============================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {

    /**
     * Primary Key. We'll use UUID for simplicity in migration or 
     * Long for standard SQL auto-increment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * The title of the document.
     */
    @Column(nullable = false)
    private String title;

    /**
     * The text content of the document.
     * We use @Lob or specify a large length for content.
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * AI-generated summary.
     */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /**
     * Status of summary generation:
     * "PENDING", "PROCESSING", "COMPLETED", "ERROR"
     */
    private String status;

    /**
     * When the document was created.
     */
    private LocalDateTime createdAt;

    /**
     * Optional field for original filename.
     */
    private String fileName;
}
