package org.vladimir.infotecs.keyvaluedb.repository;

import org.springframework.stereotype.Repository;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A repository implementation that stores key-value pairs in an in-memory {@link HashMap}.
 * <p>
 * This class is not thread-safe. If multiple threads access this repository concurrently, it
 * may lead to unpredictable behavior and data corruption. For thread-safe operations, consider
 * using synchronization mechanisms or concurrent collections.
 * </p>
 */
@Repository
public class HashMapKeyValueRepository implements KeyValueRepository {
    private final Map<String, ValueWithExpirationTime> storage;

    public HashMapKeyValueRepository() {
        storage = new HashMap<>();
    }

    public Optional<ValueWithExpirationTime> get(String key) {
        return Optional.ofNullable(storage.get(key));
    }


    public void put(String key, String value, LocalDateTime expirationTime) {
        storage.put(key, new ValueWithExpirationTime(value, expirationTime));
    }


    public boolean contains(String key) {
        return storage.containsKey(key);
    }


    public Optional<ValueWithExpirationTime> remove(String key) {
        return Optional.ofNullable(storage.remove(key));
    }


    public Map<String, ValueWithExpirationTime> getAll() {
        return new HashMap<>(storage);
    }


    public void setAll(Map<String, ValueWithExpirationTime> map) {
        storage.putAll(map);
    }


    public void clear() {
        storage.clear();
    }

    public void removeAllOutdatedPairs() {
        storage.entrySet().removeIf(e -> e.getValue().getExpirationTime().isBefore(LocalDateTime.now()));
    }

}
