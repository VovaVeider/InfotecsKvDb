package org.vladimir.infotecs.keyvaluedb.service;

import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.vladimir.infotecs.keyvaluedb.exception.IncorrectTtlValue;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.repository.DbKeyValueRepository;


import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DbKVService implements KeyValueService {

    private final DbKeyValueRepository repository;
    private final long defaultTTL;


    public DbKVService(DbKeyValueRepository keyValueRepository, long defaultTTL) {
        this.repository = keyValueRepository;
        this.defaultTTL = defaultTTL;
    }

    @Override
    @Transactional
    public void setValueByKey(@NonNull String key, @NonNull String value, long ttl) {
        if (ttl < 0) {
            throw new IncorrectTtlValue();
        }
        ttl = ttl == 0 ? defaultTTL : ttl;
        repository.put(key, value, currentTime() + ttl);
    }

    @Override
    @Transactional
    public Optional<String> getValueByKey(@NonNull String key) {
        return repository.getIfNotOutdated(key, currentTime())
                .map(ValueWithExpirationTime::getValue);
    }

    @Override
    @Transactional
    public Optional<String> deleteValueByKey(@NonNull String key) {
        return repository.removeAndReturnIfNotOutdated(key, currentTime())
                .map(ValueWithExpirationTime::getValue);
    }

    @Override
    @Transactional
    public Map<String, ValueWithExpirationTime> getDump() {
        return repository.getAll().entrySet().stream()
                .filter(e -> e.getValue().getExpirationTime() >= currentTime())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    @Transactional
    public void restoreFromDump(Map<String, ValueWithExpirationTime> map) {
        Map<String, ValueWithExpirationTime> filteredMap = map.entrySet().stream()
                .filter(e -> e.getValue().getExpirationTime() >= currentTime())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        repository.clear();
        repository.addAll(filteredMap);
    }

    @Override
    @Transactional
    public void deleteAllOutdatedPairs() {
        repository.removeAllOutdatedPairs(currentTime());
    }

    private long currentTime() {
        return System.currentTimeMillis() / 1000L;
    }
}
