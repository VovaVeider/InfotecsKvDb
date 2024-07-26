package org.vladimir.infotecs.keyvaluedb.repository;

import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;

import java.util.Map;
import java.util.Optional;

/**
 * An interface for a key-value repository.Key and not value are not null
 * Outdated key-value pairs auto removing isn't guaranteed, see implementation details
 * Expiration time is presented in unix time(long) (seconds since epoch in UTC)
 *
 */
public interface KeyValueRepository {

    Optional<ValueWithExpirationTime> get(String key);

    /**
     * Get value if current time(see implementation details) isn't greater than expiration time
     */
    Optional<ValueWithExpirationTime> getIfNotOutdated(String key);

    /**
     * Get value if provided time  isn't greater than expiration time
     *
     * @param key  Key of a pair
     * @param time current time in unix time (seconds since epoch in UTC)
     */
    Optional<ValueWithExpirationTime> getIfNotOutdated(String key, long time);

    /**
     * Put value by key
     *
     * @param key            Key of a pair
     * @param expirationTime in unix time (seconds since epoch in UTC)
     */
    void put(String key, String value, long expirationTime);


    /**
     * Remove a value by key
     *
     * @param key Key of a pair
     * @return true if key-value pair exists, otherwise false
     */
    boolean remove(String key);

    Optional<ValueWithExpirationTime> removeAndReturn(String key);

    /**
     * Remove the value by key. Returns an empty Optional if the value is not present or if it is outdated
     * (i.e., if the current time is greater than the expiration time; see implementation details)
     *
     * @param key Key of a pair
     */
    Optional<ValueWithExpirationTime> removeAndReturnIfNotOutdated(String key);

    /**
     * Remove the value by key. Returns an empty Optional if the value is not present or if it is outdated
     * (i.e., if the current time is greater than the provided time)
     *
     * @param key  Key of a pair
     * @param time current time in unix time (seconds since epoch in UTC)
     */
    Optional<ValueWithExpirationTime> removeAndReturnIfNotOutdated(String key, long time);

    /**
     * Get current state of the storage
     *
     * @return Map that represents current state of the storage
     */
    Map<String, ValueWithExpirationTime> getAll();

    /**
     * Add all key-value pairs to the storage.
     * The values for keys that are not represented in the map will remain unchanged.
     */
    void addAll(Map<String, ValueWithExpirationTime> map);

    /**
     * Remove  outdated key-value pairs
     * (i.e., if the current time is greater than the expiration time; see implementation details)
     */
    void removeAllOutdatedPairs();

    /**
     * Remove  outdated key-value pairs
     * (i.e., if the presented time is greater than the expiration time; see implementation details)
     *
     * @param time current time in unix time (seconds since epoch in UTC)
     */
    void removeAllOutdatedPairs(long time);

    /**
     * Clear repository
     */
    void clear();

    boolean contains(String key);

}

