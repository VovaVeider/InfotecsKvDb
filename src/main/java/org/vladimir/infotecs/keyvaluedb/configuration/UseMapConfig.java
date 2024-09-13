package org.vladimir.infotecs.keyvaluedb.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vladimir.infotecs.keyvaluedb.repository.HashMapKeyValueRepository;
import org.vladimir.infotecs.keyvaluedb.service.RWLSyncKvService;

@ConditionalOnProperty(name = "useDb", havingValue = "false", matchIfMissing = true)
@Configuration
public class UseMapConfig {

    @Bean
    public HashMapKeyValueRepository hashMapKeyValueRepository(){
        return new HashMapKeyValueRepository();
    }

    @Bean
    public RWLSyncKvService rwlSyncKvDbService(HashMapKeyValueRepository hashMapKeyValueRepository,
                                               @Value("${defaultTTL:200}") Long defaultTTL){
        return new RWLSyncKvService(hashMapKeyValueRepository, defaultTTL);

    }
}
