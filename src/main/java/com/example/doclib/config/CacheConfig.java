package com.example.doclib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * ============================================================
 * CACHE CONFIGURATION
 * ============================================================
 *
 * Teaching Note - Why do we cache?
 *
 * PROBLEM: Every API call hits the database. Database queries are slow
 * (network round trip + disk I/O). For frequently accessed data,
 * this is wasteful.
 *
 * SOLUTION: Cache = store results in fast memory (RAM).
 *
 * HOW IT WORKS:
 * Request 1: Cache MISS → query MongoDB → store result in cache → return result
 * Request 2: Cache HIT  → return from memory (no DB query!)     → return result
 *
 * Time difference:
 * MongoDB query: ~10-50ms
 * Cache lookup : ~0.1ms   (500x faster!)
 *
 * ANALOGY:
 * Cache is like a sticky note on your desk.
 * Instead of looking up a phone number in a phonebook every time,
 * you write it on a sticky note for quick access.
 *
 * CACHE NAMES USED IN THIS APP:
 * "documents" → stores the full document list
 * "search"    → stores search results by keyword
 *
 * @EnableCaching  → activates Spring's caching infrastructure
 * @EnableScheduling → needed for the scheduled cache eviction
 * ============================================================
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Creates the Cache Manager using simple in-memory ConcurrentHashMap.
     *
     * Teaching Note:
     * In production, you'd use Redis (distributed cache).
     * For this demo, we use the simple in-memory cache - no extra setup needed!
     *
     * Cache names must be declared here to be recognized.
     */
    @Bean
    public CacheManager cacheManager() {
        log.info("🗄️  Initializing Cache Manager with caches: [documents, search]");
        ConcurrentMapCacheManager manager = new ConcurrentMapCacheManager(
            "documents",   // used by getAllDocuments()
            "search"       // used by searchDocuments()
        );
        return manager;
    }

    /**
     * AUTO CACHE EVICTION - scheduled every 60 seconds
     *
     * Teaching Note:
     * Stale data problem: if we add a new document, the cached list
     * won't include it until the cache is cleared.
     *
     * Solutions:
     * 1. Evict on write (@CacheEvict in service methods)
     * 2. TTL (Time-To-Live) - evict after N seconds [this approach]
     *
     * This runs automatically every 60 seconds and clears the cache.
     * After clearing, the next request will re-populate it from MongoDB.
     */
    @Scheduled(fixedRate = 60000) // every 60 seconds
    @CacheEvict(value = {"documents", "search"}, allEntries = true)
    public void evictAllCaches() {
        log.info("⏰ Scheduled cache eviction triggered - all caches cleared");
        log.info("   Next request will fetch fresh data from MongoDB");
    }
}
