package com.example.doclib.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * MONITORING CONTROLLER - System Metrics API
 * ============================================================
 *
 * Teaching Note - Why do we need monitoring?
 *
 * In production, you need to know:
 * 1. Is my application healthy?
 * 2. How much memory is it using?
 * 3. How many threads are running?
 * 4. Are caches working (hit ratio)?
 * 5. How many requests per second?
 *
 * This controller exposes custom metrics for the dashboard.
 * Spring Boot Actuator (at /actuator/*) provides even more metrics.
 *
 * Teaching Note - Actuator vs Custom Metrics:
 * Actuator → framework-level metrics (JVM, HTTP, DB connections)
 * Custom   → business-level metrics (cache hits, documents processed)
 *
 * Both are valuable! This demo shows both approaches.
 * ============================================================
 */
@RestController
@RequestMapping("/api/monitor")
@CrossOrigin(origins = "*")
public class MonitoringController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringController.class);

    @Autowired
    private CacheManager cacheManager;

    // ============================================================
    // SYSTEM METRICS
    // GET /api/monitor/system
    // ============================================================

    /**
     * Returns live JVM and OS metrics.
     *
     * Teaching Note - Java Management API:
     * The java.lang.management package lets us inspect the JVM at runtime.
     * ManagementFactory provides access to various MXBeans (Managed Beans).
     *
     * MemoryMXBean    → heap and non-heap memory usage
     * ThreadMXBean    → thread count and states
     * OperatingSystemMXBean → CPU load and system info
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        log.debug("📊 System metrics requested");

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        // Memory calculations (convert bytes to MB)
        long usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
        long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
        double memoryUsagePercent = (double) usedMemoryMB / maxMemoryMB * 100;

        // Heap memory details
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);

        Map<String, Object> metrics = new HashMap<>();

        // Memory section
        Map<String, Object> memory = new HashMap<>();
        memory.put("usedMB", usedMemoryMB);
        memory.put("maxMB", maxMemoryMB);
        memory.put("totalMB", totalMemoryMB);
        memory.put("heapUsedMB", heapUsed);
        memory.put("heapMaxMB", heapMax);
        memory.put("usagePercent", Math.round(memoryUsagePercent * 10.0) / 10.0);
        metrics.put("memory", memory);

        // Thread section
        Map<String, Object> threads = new HashMap<>();
        threads.put("activeThreads", threadBean.getThreadCount());
        threads.put("daemonThreads", threadBean.getDaemonThreadCount());
        threads.put("peakThreads", threadBean.getPeakThreadCount());
        metrics.put("threads", threads);

        // System section
        Map<String, Object> system = new HashMap<>();
        system.put("availableProcessors", osBean.getAvailableProcessors());
        system.put("osName", osBean.getName());
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("uptimeSeconds", ManagementFactory.getRuntimeMXBean().getUptime() / 1000);

        // Try to get CPU usage (requires com.sun.management.OperatingSystemMXBean)
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            double systemCpuLoad = sunOsBean.getSystemCpuLoad();
            double processCpuLoad = sunOsBean.getProcessCpuLoad();
            system.put("systemCpuLoad", Math.round(systemCpuLoad * 1000.0) / 10.0);
            system.put("processCpuLoad", Math.round(processCpuLoad * 1000.0) / 10.0);
        }

        metrics.put("system", system);

        metrics.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(metrics);
    }

    // ============================================================
    // CACHE METRICS
    // GET /api/monitor/cache
    // ============================================================

    /**
     * Returns current cache information.
     *
     * Teaching Note:
     * The CacheManager lets us inspect all registered caches.
     * We can see which caches exist and what's stored in them.
     *
     * In a real app, you'd also track:
     * - cache hit rate (hits / total requests)
     * - cache eviction count
     * - cache size (number of entries)
     */
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> getCacheMetrics() {
        log.debug("🗄️  Cache metrics requested");

        Map<String, Object> cacheInfo = new HashMap<>();

        // List all cache names
        cacheInfo.put("cacheNames", cacheManager.getCacheNames());

        // Check if each cache has data
        Map<String, String> cacheStatus = new HashMap<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(cacheName);
            cacheStatus.put(cacheName, cache != null ? "active" : "inactive");
        }
        cacheInfo.put("cacheStatus", cacheStatus);

        cacheInfo.put("implementation", cacheManager.getClass().getSimpleName());
        cacheInfo.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(cacheInfo);
    }

    // ============================================================
    // HEALTH CHECK
    // GET /api/monitor/health
    // ============================================================

    /**
     * Simple custom health check endpoint.
     *
     * Teaching Note:
     * Spring Actuator's /actuator/health is more comprehensive,
     * but this shows how to build your own.
     *
     * Real health checks verify:
     * - Database connectivity
     * - External API availability
     * - Disk space
     * - Memory thresholds
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Smart Document Library");
        health.put("timestamp", System.currentTimeMillis());

        // Memory check - warn if over 80% used
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double memPercent = (double) usedMemory / runtime.maxMemory() * 100;
        health.put("memoryStatus", memPercent < 80 ? "OK" : "HIGH");
        health.put("memoryPercent", Math.round(memPercent));

        log.debug("✅ Health check: status=UP, memory={}%", Math.round(memPercent));
        return ResponseEntity.ok(health);
    }

    // ============================================================
    // EVICT CACHE MANUALLY
    // DELETE /api/monitor/cache
    // ============================================================

    /**
     * Manually clears all caches.
     * Useful for testing: clear the cache and then observe
     * the next request hitting MongoDB again.
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, String>> evictAllCaches() {
        log.info("🗑️  Manual cache eviction requested via API");

        for (String cacheName : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("   Cleared cache: '{}'", cacheName);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All caches cleared. Next requests will hit MongoDB.");

        return ResponseEntity.ok(response);
    }
}
