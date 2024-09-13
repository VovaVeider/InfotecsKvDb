package org.vladimir.infotecs.keyvaluedb.repository;


import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A repository implementation that stores key-value pairs in an in-memory {@link HashMap}.
 * This class is NOT THREAD SAFE.
 * </p>
 */

public class HashMapKeyValueRepository implements KeyValueRepository {
    private final Map<String, ValueWithExpirationTime> storage;

    public HashMapKeyValueRepository() {
        storage = new HashMap<>();
    }

    @Override
    public Optional<ValueWithExpirationTime> get(String key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public Optional<ValueWithExpirationTime> getIfNotOutdated(String key) {
        return getIfNotOutdated(key, currentTimeInSeconds());
    }

    @Override
    public Optional<ValueWithExpirationTime> getIfNotOutdated(String key, long time) {
        ValueWithExpirationTime value = storage.get(key);
        if (value != null && value.getExpirationTime() >= time) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public void put(String key, String value, long expirationTime) {
        storage.put(key, new ValueWithExpirationTime(value, expirationTime));
    }

    @Override
    public boolean contains(String key) {
        return storage.containsKey(key);
    }

    @Override
    public boolean remove(String key) {
        return null != storage.remove(key);
    }

    @Override
    public Optional<ValueWithExpirationTime> removeAndReturn(String key) {
        return Optional.ofNullable(storage.remove(key));
    }

    @Override
    public Optional<ValueWithExpirationTime> removeAndReturnIfNotOutdated(String key) {
        return removeAndReturnIfNotOutdated(key, currentTimeInSeconds());
    }

    @Override
    public Optional<ValueWithExpirationTime> removeAndReturnIfNotOutdated(String key, long time) {
        var value = storage.remove(key);
        if (value != null && value.getExpirationTime() >= time) {
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<String, ValueWithExpirationTime> getAll() {
        return new HashMap<>(storage);
    }

    @Override
    public void addAll(Map<String, ValueWithExpirationTime> map) {
        storage.putAll(map);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    public void removeAllOutdatedPairs() {
        removeAllOutdatedPairs(currentTimeInSeconds());
    }

    @Override
    public void removeAllOutdatedPairs(long time) {
        storage.entrySet().removeIf(e -> e.getValue().getExpirationTime() < time);
    }

    private long currentTimeInSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

}
