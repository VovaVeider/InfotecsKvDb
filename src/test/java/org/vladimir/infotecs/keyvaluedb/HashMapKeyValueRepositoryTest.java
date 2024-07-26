package org.vladimir.infotecs.keyvaluedb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.repository.HashMapKeyValueRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HashMapKeyValueRepositoryTest {

    private HashMapKeyValueRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HashMapKeyValueRepository();
    }

    @Test
    void testPutAndGet() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        repository.put(key, value, expirationTime);
        Optional<ValueWithExpirationTime> result = repository.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get().getValue());
        assertEquals(expirationTime, result.get().getExpirationTime());
    }

    @Test
    void testContains() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        repository.put(key, value, expirationTime);

        assertTrue(repository.contains(key));
        assertFalse(repository.contains("key2"));
    }

    @Test
    void testRemove() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        repository.put(key, value, expirationTime);
        Optional<ValueWithExpirationTime> removedValue = repository.remove(key);

        assertTrue(removedValue.isPresent());
        assertEquals(value, removedValue.get().getValue());
        assertEquals(expirationTime, removedValue.get().getExpirationTime());
        assertFalse(repository.contains(key));
    }

    @Test
    void testGetAll() {
        String key1 = "key1";
        String value1 = "value1";
        LocalDateTime expirationTime1 = LocalDateTime.now().plusDays(1);

        String key2 = "key2";
        String value2 = "value2";
        LocalDateTime expirationTime2 = LocalDateTime.now().plusDays(2);

        repository.put(key1, value1, expirationTime1);
        repository.put(key2, value2, expirationTime2);

        Map<String, ValueWithExpirationTime> allValues = repository.getAll();

        assertEquals(2, allValues.size());
        assertTrue(allValues.containsKey(key1));
        assertTrue(allValues.containsKey(key2));
    }

    @Test
    void testSetAll() {
        Map<String, ValueWithExpirationTime> initialMap = Map.of(
                "key1", new ValueWithExpirationTime("value1", LocalDateTime.now().plusDays(1)),
                "key2", new ValueWithExpirationTime("value2", LocalDateTime.now().plusDays(2))
        );

        repository.setAll(initialMap);

        Map<String, ValueWithExpirationTime> allValues = repository.getAll();
        assertEquals(2, allValues.size());
        assertEquals("value1", allValues.get("key1").getValue());
        assertEquals("value2", allValues.get("key2").getValue());
    }

    @Test
    void testClear() {
        repository.put("key1", "value1", LocalDateTime.now().plusDays(1));
        repository.put("key2", "value2", LocalDateTime.now().plusDays(2));

        repository.clear();

        assertTrue(repository.getAll().isEmpty());
    }

    @Test
    void testRemoveAllOutdatedPairs() {
        repository.put("key1", "value1", LocalDateTime.now().minusDays(1)); // Expired
        repository.put("key2", "value2", LocalDateTime.now().plusDays(1));  // Not expired

        repository.removeAllOutdatedPairs();

        Map<String, ValueWithExpirationTime> allValues = repository.getAll();

        assertEquals(1, allValues.size());
        assertFalse(allValues.containsKey("key1"));
        assertTrue(allValues.containsKey("key2"));

    }
}

