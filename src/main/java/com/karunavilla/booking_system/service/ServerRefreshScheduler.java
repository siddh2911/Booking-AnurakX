package com.karunavilla.booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServerRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ServerRefreshScheduler.class);

    @Scheduled(fixedRate = 900000) // 15 minutes = 900,000 milliseconds
    public void refreshDbServer() {
        logger.info("DB server refresh task executed. This typically involves clearing application-level caches or validating connections.");
        // Placeholder for actual DB server refresh logic:
        // - Invalidate application-level caches (e.g., Hibernate 2nd level cache)
        // - Optionally, perform a health check or re-validation of the database connection pool.
        //   (Specific implementation depends on the connection pool and ORM being used)
    }
}
