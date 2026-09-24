package com.example.case_viewer.service;

import com.example.case_viewer.exception.DatabaseCapacityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * Runs a DB operation while holding a permit from the shared database semaphore,
 * failing fast with {@link DatabaseCapacityException} (503) when none is free.
 */
@Component
public class DatabaseGuard {

    private static final Logger log = LoggerFactory.getLogger(DatabaseGuard.class);

    private final Semaphore databaseSemaphore;

    public DatabaseGuard(Semaphore databaseSemaphore) {
        this.databaseSemaphore = databaseSemaphore;
    }

    public <T> T run(Supplier<T> operation) {
        if (!databaseSemaphore.tryAcquire()) {
            log.warn("DB SEMAPHORE REJECTED - no permit available");
            throw new DatabaseCapacityException("Database capacity exceeded");
        }
        try {
            return operation.get();
        } finally {
            databaseSemaphore.release();
        }
    }
}
