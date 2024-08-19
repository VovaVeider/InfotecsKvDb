package org.vladimir.infotecs.keyvaluedb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

@Component
@ConditionalOnProperty(name = "scheduler.enable", havingValue = "true")
@EnableScheduling
public class Scheduler {

    private final KeyValueService keyValueService;
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);


    @Autowired
    public Scheduler(KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    @Scheduled(fixedDelayString = "${scheduler.fixedDelay}")
    public void deleteOutdatedKVPairs()  {
        LocalDateTime startTime = LocalDateTime.now(); // Начало измерения времени

        logger.info("Removing outdated KV pairs is started.");

        keyValueService.deleteAllOutdatedPairs();

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds();

        logger.info(String.format("Removing outdated KV pairs is finished. Time is spent: %d hours %d minutes %d seconds", hours, minutes, seconds));
    }

}

