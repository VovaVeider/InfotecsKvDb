package org.vladimir.infotecs.keyvaluedb.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vladimir.infotecs.keyvaluedb.exception.IncorrectTtlValue;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.repository.KeyValueRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * {@link KeyValueService} implementation
 * <p>
 * This class uses read-write locks to manage concurrent access to the underlying {@link KeyValueRepository}.
 * It ensures that multiple threads can read the key-value store concurrently, but only one thread can
 * write to the store at a time, while preventing any read or write operations from occurring during a write.
 * </p>
 */

public class RWLSyncKvService implements KeyValueService {

    private final KeyValueRepository repository;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private final long defaultTTL;

    @Autowired
    public RWLSyncKvService(KeyValueRepository keyValueRepository, @Value("${defaultTTL:120}") long defaultTTL) {
        this.repository = keyValueRepository;
        this.defaultTTL = defaultTTL;
    }

    @Override
    public void setValueByKey(@NonNull String key, @NonNull String value, long ttl) {
        if (ttl < 0) {
            throw new IncorrectTtlValue();
        }
        ttl = ttl == 0 ? defaultTTL : ttl;
        writeLock.lock();
        try {
            repository.put(key, value, currentTime() + ttl);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<String> getValueByKey(@NonNull String key) {
        Optional<String> result;
        Optional<ValueWithExpirationTime> optionalValue;
        readLock.lock();
        try {
            optionalValue = repository.getIfNotOutdated(key, currentTime());
            if (optionalValue.isPresent()) {
                ValueWithExpirationTime value = optionalValue.get();
                result = Optional.of(value.getValue());
            } else {
                result = Optional.empty();
            }
        } finally {
            readLock.unlock();
        }
        return result;
    }

    @Override
    public Optional<String> deleteValueByKey(@NonNull String key) {
        Optional<String> result;
        writeLock.lock();
        try {
            Optional<ValueWithExpirationTime> optionalValue = repository.removeAndReturnIfNotOutdated(key, currentTime());
            return optionalValue.map(ValueWithExpirationTime::getValue);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Map<String, ValueWithExpirationTime> getDump() {
        Map<String, ValueWithExpirationTime> tempMap;
        readLock.lock();
        try {
            tempMap = repository.getAll();
        } finally {
            readLock.unlock();
        }
        var a = tempMap.entrySet()
                .stream()
                .filter(e -> e.getValue().getExpirationTime() >= currentTime())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return a;
    }

    @Override
    public void restoreFromDump(Map<String, ValueWithExpirationTime> map) {
        Map<String, ValueWithExpirationTime> repoMap = map.entrySet()
                .stream()
                .filter(e -> e.getValue().getExpirationTime() >= currentTime())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        writeLock.lock();
        try {
            repository.clear();
            repository.addAll(repoMap);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void deleteAllOutdatedPairs() {
        writeLock.lock();
        try {
            repository.removeAllOutdatedPairs(currentTime());
        } finally {
            writeLock.unlock();
        }
    }

    private long currentTime() {
        return System.currentTimeMillis() / 1000L;
    }
}
