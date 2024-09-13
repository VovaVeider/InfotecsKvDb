package org.vladimir.infotecs.keyvaluedb;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.repository.HashMapKeyValueRepository;
import org.vladimir.infotecs.keyvaluedb.repository.KeyValueRepository;
import org.vladimir.infotecs.keyvaluedb.service.RWLSyncKvService;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RWLSyncKvDbServiceIntegrationTest {

    private KeyValueRepository repository;
    private RWLSyncKvService service;

    private static final int DEFAULT_TTL = 120;

    @BeforeEach
    void setUp() {
        repository = new HashMapKeyValueRepository();
        service = new RWLSyncKvService(repository, DEFAULT_TTL);
    }

    @Test
    void testSetValueByKey() {
        String key = "key1";
        String value = "value1";
        long ttl = 60L;
        service.setValueByKey(key, value, ttl);

        Optional<ValueWithExpirationTime> result = repository.getIfNotOutdated(key);
        assertTrue(result.isPresent());
        assertEquals(value, result.get().getValue());
    }

    @Test
    void testGetValueByKey() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() + 86400;
        repository.put(key, value, expirationTime);

        Optional<String> result = service.getValueByKey(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    @Test
    void testGetValueByKeyExpired() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() - 86400;
        repository.put(key, value, expirationTime);

        Optional<String> result = service.getValueByKey(key);

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteValueByKey() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() + 86400;
        repository.put(key, value, expirationTime);

        Optional<String> result = service.deleteValueByKey(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    @Test
    void testDeleteValueByKeyExpired() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() - 86400;
        repository.put(key, value, expirationTime);

        Optional<String> result = service.deleteValueByKey(key);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllValues() {
        String key1 = "key1";
        String value1 = "value1";
        long expirationTime1 = currentTime() + 86400;

        String key2 = "key2";
        String value2 = "value2";
        long expirationTime2 = currentTime() - 86400;

        repository.put(key1, value1, expirationTime1);
        repository.put(key2, value2, expirationTime2);

        Map<String, ValueWithExpirationTime> result = service.getDump();

        assertEquals(1, result.size());
        assertTrue(result.containsKey(key1));
        assertFalse(result.containsKey(key2));
    }

    @Test
    void testLoadAllValuesByKey() {
        Map<String, ValueWithExpirationTime> inputMap = Map.of(
                "key1", new ValueWithExpirationTime("value1", currentTime() + 60),
                "key2", new ValueWithExpirationTime("value2", currentTime() + 120),
                "key3", new ValueWithExpirationTime("value3", currentTime() - 120)
        );

        service.restoreFromDump(inputMap);

        Map<String, ValueWithExpirationTime> result = repository.getAll();
        assertEquals(2, result.size());
        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key2"));
        assertFalse(result.containsKey("key3"));
    }

    @Test
    void testDeleteAllOutdatedPairs() {
        repository.put("key1", "value1", currentTime() - 86400);
        repository.put("key2", "value2", currentTime() + 86400);

        service.deleteAllOutdatedPairs();

        Map<String, ValueWithExpirationTime> result = repository.getAll();
        assertEquals(1, result.size());
        assertTrue(result.containsKey("key2"));
    }

    private long currentTime() {
        return System.currentTimeMillis() / 1000L;
    }
}

