package com.example.doclib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * ============================================================
 * ASYNC CONFIGURATION
 * ============================================================
 *
 * Teaching Note - Why do we need Async?
 *
 * PROBLEM: Some operations take a long time (e.g., generating a
 * document summary with AI might take 5-10 seconds).
 *
 * WITHOUT Async:
 *   Client sends request → Server processes for 5 seconds → Client waits → Response
 *   The HTTP thread is BLOCKED for 5 seconds.
 *   If 100 users do this simultaneously, the server runs out of threads!
 *
 * WITH Async:
 *   Client sends request → Server starts background task → Immediately responds
 *   The background task runs in a SEPARATE THREAD POOL.
 *   The HTTP thread is FREE to handle other requests.
 *
 * @EnableAsync   → activates Spring's async processing support
 * @Configuration → marks this class as a configuration source
 *
 * THREAD POOL ANALOGY:
 * Think of it like a restaurant kitchen:
 * - Core Pool Size = permanent chefs always on duty
 * - Max Pool Size  = extra chefs called in during rush hour
 * - Queue Capacity = orders waiting when all chefs are busy
 * ============================================================
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${async.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${async.queue-capacity:100}")
    private int queueCapacity;

    /**
     * Creates and configures the thread pool for async tasks.
     *
     * Teaching Note:
     * When a method annotated with @Async is called, Spring hands the task
     * to THIS executor instead of running it on the calling thread.
     *
     * @return configured thread pool executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("⚙️  Initializing Async Thread Pool:");
        log.info("   Core Pool Size  : {}", corePoolSize);
        log.info("   Max Pool Size   : {}", maxPoolSize);
        log.info("   Queue Capacity  : {}", queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Threads always running (even when idle)
        executor.setCorePoolSize(corePoolSize);

        // Maximum threads under heavy load
        executor.setMaxPoolSize(maxPoolSize);

        // Queue size: tasks waiting when all maxPoolSize threads are busy
        executor.setQueueCapacity(queueCapacity);

        // Thread name prefix (visible in logs - helps debugging)
        executor.setThreadNamePrefix("DocLib-Async-");

        // Wait for running tasks to finish when app shuts down
        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.initialize();
        return executor;
    }
}
