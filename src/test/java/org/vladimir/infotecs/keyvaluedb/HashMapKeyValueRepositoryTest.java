package org.vladimir.infotecs.keyvaluedb;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.repository.HashMapKeyValueRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HashMapKeyValueRepositoryTest {

    private HashMapKeyValueRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HashMapKeyValueRepository();

    }

    private long toUnixTime(LocalDateTime dateTime) {
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.getEpochSecond();
    }

    @Test
    void testPutAndGet() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        repository.put(key, value, toUnixTime(expirationTime));
        Optional<ValueWithExpirationTime> result = repository.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get().getValue());
        assertEquals(toUnixTime(expirationTime), result.get().getExpirationTime());
    }

    @Test
    void testPutAndGetIfNotOutdated() {
        String key = "key1";
        String value = "value1";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureExpirationTime = now.plusDays(1);
        LocalDateTime pastExpirationTime = now.minusDays(1);

        repository.put(key, value, toUnixTime(futureExpirationTime));
        assertTrue(repository.getIfNotOutdated(key).isPresent());

        repository.put(key, value, toUnixTime(pastExpirationTime));
        assertFalse(repository.getIfNotOutdated(key).isPresent());

    }

    @Test
    void testPutAndGetIfNotOutdatedWithTime() {
        String key = "key1";
        String value = "value1";

        LocalDateTime now = LocalDateTime.now();

        repository.put(key, value, toUnixTime(now.plusDays(1)));
        assertTrue(repository.getIfNotOutdated(key, toUnixTime(now)).isPresent());

        repository.put(key, value, toUnixTime(now.plusDays(1)));
        assertFalse(repository.getIfNotOutdated(key, toUnixTime(now.plusDays(2))).isPresent());

    }

    @Test
    void testPutAndCheckContains() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        repository.put(key, value, toUnixTime(expirationTime));

        assertTrue(repository.contains(key));
        assertFalse(repository.contains("key2"));
    }

    @Test
    void testRemove() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        repository.put(key, value, toUnixTime(expirationTime));
        boolean removed = repository.remove(key);

        assertTrue(removed);
        assertFalse(repository.contains(key));
    }

    @Test
    void testRemoveNonExistentKey() {
        boolean removed = repository.remove("nonexistentKey");
        assertFalse(removed);
    }

    @Test
    void testRemoveAndReturn() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        repository.put(key, value, toUnixTime(expirationTime));
        Optional<ValueWithExpirationTime> removedValue = repository.removeAndReturn(key);

        assertTrue(removedValue.isPresent());
        assertEquals(value, removedValue.get().getValue());
        assertEquals(toUnixTime(expirationTime), removedValue.get().getExpirationTime());
        assertFalse(repository.contains(key));
    }

    @Test
    void testRemoveAndReturnIfNotOutdated() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureExpirationTime = now.plusDays(1);
        LocalDateTime pastExpirationTime = now.minusDays(1);

        repository.put("key1", "value1", toUnixTime(futureExpirationTime));
        repository.put("key2", "value2", toUnixTime(pastExpirationTime));

        assertTrue(repository.removeAndReturnIfNotOutdated("key1").isPresent());
        assertFalse(repository.removeAndReturnIfNotOutdated("key2").isPresent());
    }

    @Test
    void testGetAll() {
        LocalDateTime now = LocalDateTime.now();
        repository.put("key1", "value1", toUnixTime(now.plusDays(1)));
        repository.put("key2", "value2", toUnixTime(now.plusDays(2)));

        Map<String, ValueWithExpirationTime> allValues = repository.getAll();

        assertEquals(2, allValues.size());
        assertTrue(allValues.containsKey("key1"));
        assertTrue(allValues.containsKey("key2"));
        assertEquals("value1", allValues.get("key1").getValue());
        assertEquals("value2", allValues.get("key2").getValue());
    }

    @Test
    void testAddAll() {
        Map<String, ValueWithExpirationTime> initialMap = new HashMap<>();
        initialMap.put("key1", new ValueWithExpirationTime("value1", toUnixTime(LocalDateTime.now().plusDays(1))));
        initialMap.put("key2", new ValueWithExpirationTime("value2", toUnixTime(LocalDateTime.now().plusDays(2))));

        repository.addAll(initialMap);

        Map<String, ValueWithExpirationTime> allValues = repository.getAll();
        assertEquals(2, allValues.size());
        assertEquals("value1", allValues.get("key1").getValue());
        assertEquals("value2", allValues.get("key2").getValue());
    }

    @Test
    void testClear() {
        repository.put("key1", "value1", toUnixTime(LocalDateTime.now().plusDays(1)));
        repository.put("key2", "value2", toUnixTime(LocalDateTime.now().plusDays(2)));

        repository.clear();

        assertTrue(repository.getAll().isEmpty());
    }

    @Test
    void testRemoveAllOutdatedPairs() {
        LocalDateTime now = LocalDateTime.now();
        repository.put("key1", "value1", toUnixTime(now.minusDays(1))); // Expired
        repository.put("key2", "value2", toUnixTime(now.plusDays(1)));  // Not expired

        repository.removeAllOutdatedPairs();

        Map<String, ValueWithExpirationTime> allValues = repository.getAll();

        assertEquals(1, allValues.size());
        assertFalse(allValues.containsKey("key1"));
        assertTrue(allValues.containsKey("key2"));
    }

    @Test
    void testRemoveAllOutdatedPairsWithCustomTime() {
        LocalDateTime now = LocalDateTime.now();
        repository.put("key1", "value1", toUnixTime(now.minusDays(1))); // Expired
        repository.put("key2", "value2", toUnixTime(now.plusDays(1)));  // Not expired

        repository.removeAllOutdatedPairs(toUnixTime(now));

        Map<String, ValueWithExpirationTime> allValues = repository.getAll();

        assertEquals(1, allValues.size());
        assertFalse(allValues.containsKey("key1"));
        assertTrue(allValues.containsKey("key2"));
    }

    @Test
    void testGetIfNotOutdatedWithCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        repository.put("key1", "value1", toUnixTime(now.plusDays(1)));
        repository.put("key2", "value2", toUnixTime(now.minusDays(1)));  // Expired

        assertTrue(repository.getIfNotOutdated("key1").isPresent());
        assertFalse(repository.getIfNotOutdated("key2").isPresent());
    }

    @Test
    void testAddAllEmptyMap() {
        repository.addAll(new HashMap<>());

        assertTrue(repository.getAll().isEmpty());
    }

    @Test
    void testPutWithSameKey() {
        String key = "key1";
        LocalDateTime now = LocalDateTime.now();
        repository.put(key, "value1", toUnixTime(now.plusDays(1)));
        repository.put(key, "value2", toUnixTime(now.plusDays(2)));

        Optional<ValueWithExpirationTime> result = repository.get(key);
        assertTrue(result.isPresent());
        assertEquals("value2", result.get().getValue());
    }
}
