package org.vladimir.infotecs.keyvaluedb.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.vladimir.infotecs.keyvaluedb.model.KvPair;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DbKeyValueRepository implements KeyValueRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ValueWithExpirationTime> valueWithExpTimeRowMapper;
    private final RowMapper<KvPair> kvPairRowMapper;

    public DbKeyValueRepository(JdbcTemplate jdbcTemplate,
                                RowMapper<ValueWithExpirationTime> rowMapper,
                                RowMapper<KvPair> kvPairRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.valueWithExpTimeRowMapper = rowMapper;
        this.kvPairRowMapper = kvPairRowMapper;
    }

    private Timestamp currentTimestamp() {
        return Timestamp.from(Instant.now());
    }

    @Override
    public Optional<ValueWithExpirationTime> get(String key) {
        var sql = """
                SELECT * FROM "KEY_VALUE_TABLE" WHERE "KEY" = ?
                """;
        var result = jdbcTemplate.query(sql, new Object[]{key}, valueWithExpTimeRowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<ValueWithExpirationTime> getIfNotOutdated(String key) {
        var sql = """
                SELECT * FROM "KEY_VALUE_TABLE" WHERE "KEY" = ? AND "EXPIRATION_TIME" >= ?
                """;
        var currentTime = currentTimestamp();
        var result = jdbcTemplate.query(sql, new Object[]{key, currentTime}, valueWithExpTimeRowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<ValueWithExpirationTime> getIfNotOutdated(String key, long time) {
        var sql = """
                SELECT * FROM "KEY_VALUE_TABLE" WHERE "KEY" = ? AND "EXPIRATION_TIME" >= ?
                """;
        var timestamp = new Timestamp(time * 1000L);
        var result = jdbcTemplate.query(sql, new Object[]{key, timestamp}, valueWithExpTimeRowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public void put(String key, String value, long expirationTime) {
        var sql = """
                MERGE INTO "KEY_VALUE_TABLE" ("KEY", "VALUE", "EXPIRATION_TIME")
                KEY ("KEY")
                VALUES (?, ?, ?)
                """;
        var timestamp = new Timestamp(expirationTime * 1000L);
        jdbcTemplate.update(sql, key, value, timestamp);
    }

    @Override
    public boolean remove(String key) {
        var sql = "DELETE FROM \"KEY_VALUE_TABLE\" WHERE \"KEY\" = ?";
        int rowsAffected = jdbcTemplate.update(sql, key);
        return rowsAffected > 0;
    }

    @Override
    public Optional<ValueWithExpirationTime> removeAndReturn(String key) {
        var valueOpt = get(key);
        if (valueOpt.isPresent()) {
            remove(key);
        }
        return valueOpt;
    }

    @Override
    public Optional<ValueWithExpirationTime> removeAndReturnIfNotOutdated(String key) {
        return removeAndReturnIfNotOutdated(key, currentTimestamp().getTime());
    }

    @Override
    public Optional<ValueWithExpirationTime> removeAndReturnIfNotOutdated(String key, long time) {
        var timestamp = new Timestamp(time);
        var valueOpt = getIfNotOutdated(key, timestamp.getTime());
        if (valueOpt.isPresent()) {
            remove(key);
        }
        return valueOpt;
    }

    @Override
    public Map<String, ValueWithExpirationTime> getAll() {
        var sql = "SELECT * FROM \"KEY_VALUE_TABLE\"";
        var resultList = jdbcTemplate.query(sql, kvPairRowMapper);
        Map<String, ValueWithExpirationTime> resultMap = new HashMap<>();
        for (KvPair value : resultList) {
            resultMap.put(value.getKey(), value.getValue());
        }
        return resultMap;
    }

    @Override
    public void addAll(Map<String, ValueWithExpirationTime> map) {
        var sql = """
                MERGE INTO "KEY_VALUE_TABLE" ("KEY", "VALUE", "EXPIRATION_TIME")
                KEY ("KEY")
                VALUES (?, ?, ?)
                """;
        for (var entry : map.entrySet()) {
            var timestamp = new Timestamp(entry.getValue().getExpirationTime() * 1000L);
            jdbcTemplate.update(sql, entry.getKey(), entry.getValue().getValue(), timestamp);
        }
    }

    @Override
    public void clear() {
        var sql = "DELETE FROM \"KEY_VALUE_TABLE\"";
        jdbcTemplate.update(sql);
    }

    @Override
    public void removeAllOutdatedPairs() {
        removeAllOutdatedPairs(currentTimestamp().getTime());
    }

    @Override
    public void removeAllOutdatedPairs(long time) {
        var sql = "DELETE FROM \"KEY_VALUE_TABLE\" WHERE \"EXPIRATION_TIME\" < ?";
        var timestamp = new Timestamp(time);
        jdbcTemplate.update(sql, timestamp);
    }

    @Override
    public boolean contains(String key) {
        var sql = "SELECT COUNT(*) FROM \"KEY_VALUE_TABLE\" WHERE \"KEY\" = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{key}, Integer.class);
        return count != null && count > 0;
    }
}
