package org.vladimir.infotecs.keyvaluedb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.vladimir.infotecs.keyvaluedb.exception.IncorrectTtlValue;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.repository.KeyValueRepository;
import org.vladimir.infotecs.keyvaluedb.service.RWLSyncKvDbService;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RWLSyncKvDbServiceTest {

    private KeyValueRepository repository;
    private RWLSyncKvDbService service;
    private static final int DEFAULT_TTL = 120;

    @BeforeEach
    void setUp() {
        repository = mock(KeyValueRepository.class);
        service = new RWLSyncKvDbService(repository, DEFAULT_TTL);
    }

    @Test
    void testSetValueByKey() {
        String key = "key1";
        String value = "value1";
        long ttl = 60L;
        service.setValueByKey(key, value, ttl);
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(repository, times(1)).put(eq(key), eq(value), captor.capture());
        long expirationTime = captor.getValue();
        assertTrue(expirationTime > currentTime());
    }

    @Test
    void testGetValueByKey() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() + 86400;

        when(repository.getIfNotOutdated(eq(key), longThat(currTime -> currTime <= expirationTime)))
                .thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));

        Optional<String> result = service.getValueByKey(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());

        verify(repository, never()).remove(eq(key));
        verify(repository, never()).removeAndReturnIfNotOutdated(eq(key), longThat(currTime -> currTime >= currentTime()));

    }

    @Test
    void testGetValueByKeyExpired() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() - 86400;

        when(repository.getIfNotOutdated(eq(key)))
                .thenReturn(Optional.empty());
        when(repository.getIfNotOutdated(eq(key), longThat(currTime -> currTime <= expirationTime)))
                .thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));
        when(repository.getIfNotOutdated(eq(key), longThat(currTime -> currTime > expirationTime)))
                .thenReturn(Optional.empty());

        Optional<String> result = service.getValueByKey(key);

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteValueByKey() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() + 86400;

        when(repository.removeAndReturnIfNotOutdated(eq(key), longThat(currTime -> currTime <= expirationTime)))
                .thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));
        when(repository.removeAndReturnIfNotOutdated(eq(key), longThat(currTime -> currTime > expirationTime)))
                .thenReturn(Optional.empty());

        Optional<String> result = service.deleteValueByKey(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());

        try {
            verify(repository, times(1)).removeAndReturnIfNotOutdated(eq(key), longThat(currTime -> currTime <= expirationTime));
        } catch (MockitoAssertionError e) {
            verify(repository, times(1)).removeAndReturn(eq(key));
        }

    }

    @Test
    void testDeleteValueByKeyExpired() {
        String key = "key1";
        String value = "value1";
        long expirationTime = currentTime() - 86400;

        when(repository.getIfNotOutdated(eq(key), longThat(currTime -> currTime <= expirationTime)))
                .thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));
        when(repository.getIfNotOutdated(eq(key), longThat(currTime -> currTime > expirationTime)))
                .thenReturn(Optional.empty());

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

        when(repository.getAll()).thenReturn(Map.of(
                key1, new ValueWithExpirationTime(value1, expirationTime1),
                key2, new ValueWithExpirationTime(value2, expirationTime2)
        ));

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
                "key3", new ValueWithExpirationTime("value2", currentTime() + -120)
        );

        service.restoreFromDump(inputMap);

        ArgumentCaptor<Map<String, ValueWithExpirationTime>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository, times(1)).addAll(captor.capture());

        Map<String, ValueWithExpirationTime> capturedMap = captor.getValue();
        assertEquals(2, capturedMap.size());
    }

    @Test
    void testDeleteAllOutdatedPairs() {
        service.deleteAllOutdatedPairs();
        verify(repository, times(1))
                .removeAllOutdatedPairs(longThat(currTime -> currTime <= currentTime()));
    }

    @Test
    void testSetValueByKeyWithIncorrectTTL() {
        assertThrows(IncorrectTtlValue.class, () -> service.setValueByKey("key1", "value1", -1L));
    }


    private long currentTime() {
        return System.currentTimeMillis() / 1000L;
    }
}
