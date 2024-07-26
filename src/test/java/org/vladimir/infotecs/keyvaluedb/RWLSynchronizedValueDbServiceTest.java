package org.vladimir.infotecs.keyvaluedb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.vladimir.infotecs.keyvaluedb.exception.IncorrectTtlValue;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithTtl;
import org.vladimir.infotecs.keyvaluedb.repository.KeyValueRepository;
import org.vladimir.infotecs.keyvaluedb.service.RWLSynchronizedValueDbService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RWLSynchronizedValueDbServiceTest {


    private KeyValueRepository repository;
    private RWLSynchronizedValueDbService service;
    private static final int DEFAULT_TTL = 2;

    @BeforeEach
    void setUp() {
        repository = mock(KeyValueRepository.class);
        //Turn off auto removing outdated KV pairs
        service = new RWLSynchronizedValueDbService(repository, DEFAULT_TTL, false);
    }

    @Test
    void testSetValueByKey() {
        String key = "key1";
        String value = "value1";
        Long ttl = 60L;
        service.setValueByKey(key, value, ttl);
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository, times(1)).put(eq(key), eq(value), captor.capture());
        LocalDateTime expirationTime = captor.getValue();
        assertTrue(expirationTime.isAfter(LocalDateTime.now()));
    }

    @Test
    void testGetValueByKey() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        when(repository.get(key)).thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));

        Optional<String> result = service.getValueByKey(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());

        verify(repository, never()).remove(key);
    }

    @Test
    void testGetValueByKeyExpired() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().minusDays(1);

        when(repository.get(key)).thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));

        Optional<String> result = service.getValueByKey(key);

        assertFalse(result.isPresent());

        verify(repository, never()).remove(key);
    }

    @Test
    void testDeleteValueByKey() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        when(repository.remove(key)).thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));

        Optional<String> result = service.deleteValueByKey(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());

        verify(repository, times(1)).remove(key);
    }

    @Test
    void testDeleteValueByKeyExpired() {
        String key = "key1";
        String value = "value1";
        LocalDateTime expirationTime = LocalDateTime.now().minusDays(1);

        when(repository.remove(key)).thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));

        Optional<String> result = service.deleteValueByKey(key);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllValues() {
        String key1 = "key1";
        String value1 = "value1";
        LocalDateTime expirationTime1 = LocalDateTime.now().plusDays(1);

        String key2 = "key2";
        String value2 = "value2";
        LocalDateTime expirationTime2 = LocalDateTime.now().minusDays(1);

        when(repository.getAll()).thenReturn(Map.of(
                key1, new ValueWithExpirationTime(value1, expirationTime1),
                key2, new ValueWithExpirationTime(value2, expirationTime2)
        ));

        Map<String, String> result = service.getAllValues();

        assertEquals(1, result.size());
        assertTrue(result.containsKey(key1));
        assertFalse(result.containsKey(key2));

    }

    @Test
    void testLoadAllValuesByKey() {
        Map<String, ValueWithTtl> inputMap = Map.of(
                "key1", new ValueWithTtl("value1", 60L),
                "key2", new ValueWithTtl("value2", 120L)
        );

        service.loadAllValuesByKey(inputMap);

        ArgumentCaptor<Map<String, ValueWithExpirationTime>> captor = ArgumentCaptor.forClass(Map.class);
        verify(repository, times(1)).setAll(captor.capture());

        Map<String, ValueWithExpirationTime> capturedMap = captor.getValue();
        assertEquals(2, capturedMap.size());
    }

    @Test
    void testDeleteAllOutdatedPairs() {
        service.deleteAllOutdatedPairs();
        verify(repository, times(1)).removeAllOutdatedPairs();
    }

    @Test
    void testSetValueByKeyWithIncorrectTTL() {
        assertThrows(IncorrectTtlValue.class, () -> service.setValueByKey("key1", "value1", -1L));
    }

    /**
     * Testing auto removing outdated KV pairs
     */
    @Nested
    class TestAutoRemoveMode {
        @BeforeEach
        public void setUp() {
            repository = mock(KeyValueRepository.class);
            service = new RWLSynchronizedValueDbService(repository, DEFAULT_TTL, true);
        }

        @Test
        void testGetValueByKey() {
            String key = "key1";
            String value = "value1";
            LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

            when(repository.get(key)).thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));

            Optional<String> result = service.getValueByKey(key);

            assertTrue(result.isPresent());
            assertEquals(value, result.get());

            verify(repository, never()).remove(key);
        }

        @Test
        void testGetValueByKeyExpired() {
            String key = "key1";
            String value = "value1";
            LocalDateTime expirationTime = LocalDateTime.now().minusDays(1);

            when(repository.get(key)).thenReturn(Optional.of(new ValueWithExpirationTime(value, expirationTime)));

            Optional<String> result = service.getValueByKey(key);

            assertFalse(result.isPresent());

            verify(repository, times(1)).remove(key);
        }

        @Test
        void testGetAllValues() {
            String key1 = "key1";
            String value1 = "value1";
            LocalDateTime expirationTime1 = LocalDateTime.now().plusDays(1);

            String key2 = "key2";
            String value2 = "value2";
            LocalDateTime expirationTime2 = LocalDateTime.now().minusDays(1);

            when(repository.getAll()).thenReturn(Map.of(
                    key1, new ValueWithExpirationTime(value1, expirationTime1),
                    key2, new ValueWithExpirationTime(value2, expirationTime2)
            ));

            Map<String, String> result = service.getAllValues();
            try {
                verify(repository, times(1)).remove(key2);
            } catch (MockitoAssertionError e) {
                verify(repository, times(1)).removeAllOutdatedPairs();
            }

            assertEquals(1, result.size());
            assertTrue(result.containsKey(key1));
            assertFalse(result.containsKey(key2));

        }
    }
}