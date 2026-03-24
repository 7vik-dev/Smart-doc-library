package com.example.doclib.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.MongoDocument;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * ============================================================
 * DOCUMENT MODEL - MongoDB Collection
 * ============================================================
 *
 * This class maps to a MongoDB collection called "documents".
 * Each field becomes a key in the MongoDB document (JSON object).
 *
 * Teaching Note:
 * @Document  → marks this class as a MongoDB collection
 * @Id        → marks the unique identifier field (MongoDB uses _id)
 * @Indexed   → creates a database index for faster searches on 'title'
 *
 * Lombok annotations auto-generate boilerplate:
 * @Data          → generates getters, setters, toString, equals, hashCode
 * @Builder       → enables DocumentBuilder pattern
 * @NoArgsConstructor / @AllArgsConstructor → generate constructors
 * ============================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@MongoDocument(collection = "documents")
public class Document {

    /**
     * MongoDB auto-generates this unique ID.
     * It looks like: "507f1f77bcf86cd799439011"
     */
    @Id
    private String id;

    /**
     * The title of the document.
     * @Indexed makes title searches faster by building a B-tree index.
     */
    @Indexed
    private String title;

    /**
     * The text content of the document.
     */
    private String content;

    /**
     * Original filename when uploaded (e.g., "report.pdf")
     */
    private String fileName;

    /**
     * When the document was uploaded.
     * Automatically set at upload time.
     */
    private LocalDateTime uploadedDate;

    /**
     * AI-generated summary (populated asynchronously).
     * Will be null until async processing completes.
     */
    private String summary;

    /**
     * Status of summary generation:
     * "NONE"       - no summary requested
     * "PROCESSING" - async task is running
     * "COMPLETED"  - summary is ready
     */
    private String summaryStatus;
}
