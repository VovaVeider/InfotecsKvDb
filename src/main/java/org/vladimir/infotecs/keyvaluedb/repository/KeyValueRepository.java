package org.vladimir.infotecs.keyvaluedb.repository;

import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * An interface for a key-value repository that supports basic CRUD operations and expiration management.
 * Key value pairs that have expired will be deleted
 * <p>
 * </p>
 */
public interface KeyValueRepository {

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key whose associated value is to be returned
     * @return an {@link Optional} containing the {@link ValueWithExpirationTime} associated with the key, or an empty {@link Optional} if the key does not exist
     */
    Optional<ValueWithExpirationTime> get(String key);

    /**
     * Associates the specified value with the specified key and sets an expiration time.
     *
     * @param key            the key with which the specified value is to be associated
     * @param value          the value to be associated with the key
     * @param expirationTime the time at which the value should expire
     */
    void put(String key, String value, LocalDateTime expirationTime);

    /**
     * Removes the value associated with the specified key.
     *
     * @param key the key whose associated value is to be removed
     * @return an {@link Optional} containing the removed {@link ValueWithExpirationTime}, or an empty {@link Optional} if the key did not exist
     */
    Optional<ValueWithExpirationTime> remove(String key);

    /**
     * Returns a copy of all key-value mappings in the repository.
     *
     * @return a {@link Map} containing all key-value pairs in the repository, where the keys are the keys and the values are the {@link ValueWithExpirationTime} objects
     */
    Map<String, ValueWithExpirationTime> getAll();

    /**
     * Sets the key-value mappings from the specified map into this repository.
     *
     * @param map a {@link Map} containing key-value pairs to be added or updated in the repository
     */
    void setAll(Map<String, ValueWithExpirationTime> map);

    /**
     * Removes all key-value pairs from the repository where the expiration time has passed.
     */
    void removeAllOutdatedPairs();

    /**
     * Clears all key-value mappings from the repository.
     */
    void clear();

    /**
     * Checks if a value is associated with the specified key.
     *
     * @param key the key whose presence in the repository is to be tested
     * @return {@code true} if a value is associated with the key; {@code false} otherwise
     */
    boolean contains(String key);
}

