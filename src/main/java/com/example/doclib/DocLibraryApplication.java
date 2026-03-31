package com.example.doclib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * SMART DOCUMENT LIBRARY - Main Application Class
 * ============================================================
 *
 * @SpringBootApplication is a shortcut for three annotations:
 *
 * @SpringBootConfiguration  → marks this as a configuration class
 * @EnableAutoConfiguration  → Spring Boot auto-configures beans
 *                             (e.g., sees MongoDB on classpath → sets up MongoTemplate)
 * @ComponentScan            → scans for @Service, @Controller, @Repository
 *                             in this package and sub-packages
 *
 * Teaching Note - How Spring Boot Auto-Configuration Works:
 * When Spring Boot starts, it reads all META-INF/spring.factories files.
 * Based on what's on the classpath and your application.properties,
 * it automatically configures:
 * - MongoDB connection (because spring-boot-starter-data-mongodb is in pom.xml)
 * - Cache manager (because spring-boot-starter-cache is in pom.xml)
 * - Actuator endpoints (because spring-boot-starter-actuator is in pom.xml)
 * - Async thread pool (because @EnableAsync is in AsyncConfig.java)
 *
 * You don't need to write any of this setup code yourself!
 * ============================================================
 */
@SpringBootApplication
public class DocLibraryApplication {

    private static final Logger log = LoggerFactory.getLogger(DocLibraryApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DocLibraryApplication.class, args);

        // Print helpful startup message
        log.info("");
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║       Smart Document Library - STARTED!          ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  API Base URL : /api                             ║");
        log.info("║  Dashboard    : /                                ║");
        log.info("║  Actuator     : /actuator                        ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  KEY ENDPOINTS:                                   ║");
        log.info("║  GET    /api/documents           (with caching)   ║");
        log.info("║  POST   /api/documents           (create doc)     ║");
        log.info("║  GET    /api/documents/search    (with caching)   ║");
        log.info("║  POST   /api/documents/{id}/summary  (async!)     ║");
        log.info("║  GET    /api/monitor/system      (JVM metrics)    ║");
        log.info("╠══════════════════════════════════════════════════╣");
        log.info("║  ACTUATOR MONITORING:                             ║");
        log.info("║  /actuator/health    → app + PostgreSQL health    ║");
        log.info("║  /actuator/metrics   → JVM, HTTP, cache metrics   ║");
        log.info("║  /actuator/loggers   → view/change log levels     ║");
        log.info("╚══════════════════════════════════════════════════╝");
        log.info("");
    }
}
