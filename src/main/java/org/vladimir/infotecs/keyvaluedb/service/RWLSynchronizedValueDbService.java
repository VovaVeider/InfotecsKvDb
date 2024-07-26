package org.vladimir.infotecs.keyvaluedb.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vladimir.infotecs.keyvaluedb.exception.IncorrectTtlValue;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithTtl;
import org.vladimir.infotecs.keyvaluedb.repository.KeyValueRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * {@link KeyValueDbService} implementation
 * <p>
 * This class uses read-write locks to manage concurrent access to the underlying {@link KeyValueRepository}.
 * It ensures that multiple threads can read the key-value store concurrently, but only one thread can
 * write to the store at a time, while preventing any read or write operations from occurring during a write.
 * </p>
 */
@Service
public class RWLSynchronizedValueDbService implements KeyValueDbService {

    private final KeyValueRepository repository;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private final long defaultTTL;
    private static final long DEFAULT_TTL_VALUE = 120;
    private final boolean autoRemoveOutdated;

    @Autowired
    public RWLSynchronizedValueDbService(KeyValueRepository hashMapKeyValueRepository,
                                    @Value("${defaultTTL:120}") Integer defaultTTL,
                                    @Value("${autoRemoveOutdated:false}") boolean autoRemoveOutdated) {
        this.repository = hashMapKeyValueRepository;
        this.defaultTTL = (defaultTTL != null) ? defaultTTL : DEFAULT_TTL_VALUE;
        this.autoRemoveOutdated = autoRemoveOutdated;
    }

    public void setValueByKey(@NonNull String key, @NonNull String value, Long ttl) {
        if (ttl == null) {
            ttl = defaultTTL;
        } else if (ttl < 1) {
            throw new IncorrectTtlValue();
        }
        writeLock.lock();
        try {
            repository.put(key, value, LocalDateTime.now().plusSeconds(ttl));
        } finally {
            writeLock.unlock();
        }
    }

    public Optional<String> getValueByKey(@NonNull String key) {
        Optional<String> result;
        Optional<ValueWithExpirationTime> optionalValue;
        boolean outdated = false;

        readLock.lock();
        try {
            optionalValue = repository.get(key);
        } finally {
            readLock.unlock();
        }
        if (optionalValue.isPresent()) {
            ValueWithExpirationTime value = optionalValue.get();
            if (value.getExpirationTime().isBefore(LocalDateTime.now())) {
                outdated = true;
                result = Optional.empty();
            } else {
                result = Optional.of(value.getValue());
            }
        } else {
            result = Optional.empty();
        }

        if (outdated && autoRemoveOutdated) {
            writeLock.lock();
            try {
                repository.remove(key);
            } finally {
                writeLock.unlock();
            }
        }
        return result;
    }

    public Optional<String> deleteValueByKey(@NonNull String key) {
        writeLock.lock();
        try {
            Optional<ValueWithExpirationTime> optionalValue = repository.remove(key);
            if (optionalValue.isPresent()) {
                ValueWithExpirationTime value = optionalValue.get();
                if (value.getExpirationTime().isAfter(LocalDateTime.now())) {
                    return Optional.of(value.getValue());
                }
            }
            return Optional.empty();
        } finally {
            writeLock.unlock();
        }
    }

    public Map<String, String> getAllValues() {
        Map<String, ValueWithExpirationTime> tempMap;

        //Read dump from repository
        readLock.lock();
        try {
            tempMap = repository.getAll();
        } finally {
            readLock.unlock();
        }
        Map<String, String> resultMap = tempMap.entrySet()
                .stream()
                .filter(e -> e.getValue().getExpirationTime().isAfter(LocalDateTime.now()))
                .map(e -> Map.entry(e.getKey(), e.getValue().getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //Remove outdated KV pairs if set flag
        if (autoRemoveOutdated && resultMap.size() < tempMap.size()) {
            writeLock.lock();
            try {
                repository.removeAllOutdatedPairs();
            } finally {
                writeLock.unlock();
            }
        }

        return resultMap;
    }

    public void loadAllValuesByKey(Map<String, ValueWithTtl> map) {
        Map<String, ValueWithExpirationTime> repoMap = map.entrySet()
                .stream()
                .peek(e -> {
                    Long ttl = e.getValue().getTtl();
                    if (ttl != null && ttl <= 0) {
                        throw new IncorrectTtlValue();
                    }
                })
                .map(e -> Map.entry(e.getKey(),
                        new ValueWithExpirationTime(
                                e.getValue().getValue(),
                                LocalDateTime.now().plusSeconds(
                                        Objects.requireNonNullElse(e.getValue().getTtl(), defaultTTL)
                                )
                        )
                ))
                .filter(e -> e.getValue().getExpirationTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        writeLock.lock();
        try {
            repository.setAll(repoMap);
        } finally {
            writeLock.unlock();
        }
    }

    public void deleteAllOutdatedPairs() {
        writeLock.lock();
        try {
            repository.removeAllOutdatedPairs();
        } finally {
            writeLock.unlock();
        }
    }
}
