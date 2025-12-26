package com.karunavilla.booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServerRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ServerRefreshScheduler.class);

    @Scheduled(fixedRate = 240000) // 4 minutes
    public void refreshDbServer() {
        logger.info("DB server refresh task executed to keep the application alive.");
    }
}
