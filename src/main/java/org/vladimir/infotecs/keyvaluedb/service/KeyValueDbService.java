package org.vladimir.infotecs.keyvaluedb.service;

import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithTtl;

import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing a key-value database with support for time-to-live (TTL) expiration.
 * <p>
 * This interface defines operations for setting, retrieving, deleting, and managing key-value pairs in a database.
 * The values can have an associated time-to-live (TTL), after which they are considered expired and should be removed.
 * </p>
 */
public interface KeyValueDbService {

    /**
     * Sets the value associated with the specified key, with an optional time-to-live (TTL).
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the key
     * @param ttl the time-to-live in seconds; if {@code null} or {@code 0}, the value will not expire
     */
    void setValueByKey(String key, String value, long ttl);

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key whose associated value is to be returned
     * @return an {@link Optional} containing the value associated with the key, or an empty {@link Optional} if the key does not exist or the value has expired
     */
    Optional<String> getValueByKey(String key);

    /**
     * Deletes the value associated with the specified key.
     *
     * @param key the key whose associated value is to be removed
     * @return an {@link Optional} containing the removed value, or an empty {@link Optional} if the key did not exist
     */
    Optional<String> deleteValueByKey(String key);

    /**
     * Returns a map of all key-value pairs in the database.
     * <p>
     * The returned map contains all current key-value pairs that have not expired.
     * </p>
     *
     * @return a {@link Map} containing all key-value pairs, where the keys are the keys and the values are the associated values
     */
    Map<String, ValueWithExpirationTime> getDump();

    /**
     * Loads all key-value pairs into the database from the provided map.
     * <p>
     * This method will replace existing values with the new values from the provided map.
     * </p>
     *
     * @param map a {@link Map} containing key-value pairs to be loaded into the database, where the values are wrapped in {@link ValueWithTtl}
     */
    void restoreFromDump(Map<String, ValueWithExpirationTime> map);

    /**
     * Removes all key-value pairs where the TTL has expired.
     * <p>
     * This method ensures that any entries with expired TTL values are removed from the database.
     * </p>
     */
    void deleteAllOutdatedPairs();
}

