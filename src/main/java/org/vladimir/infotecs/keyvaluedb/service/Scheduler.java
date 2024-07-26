package org.vladimir.infotecs.keyvaluedb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
@ConditionalOnProperty(name = "scheduler.enable", havingValue = "true")
@EnableScheduling
public class Scheduler {

    private final KeyValueDbService keyValueDbService;
    private Logger logger;

    @Autowired
    public Scheduler(KeyValueDbService keyValueDbService) {
        this.keyValueDbService = keyValueDbService;
    }

    @Scheduled(fixedDelayString = "${scheduler.fixedDelay}")
    public void deleteOutdatedKVPairs()  {
        LocalDateTime startTime = LocalDateTime.now(); // Начало измерения времени

        if (logger != null) {
            logger.info("Removing outdated KV pairs is started. Time " + startTime);
        }

        keyValueDbService.deleteAllOutdatedPairs();

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds();

        if (logger != null) {
            logger.info(String.format("Removing outdated KV pairs is finished. Time %d hours %d minutes %d seconds", hours, minutes, seconds));

        }
    }

    @Autowired
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}

