package org.vladimir.infotecs.keyvaluedb.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.vladimir.infotecs.keyvaluedb.model.KvPair;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.vladimir.infotecs.keyvaluedb.repository.DbKeyValueRepository;
import org.vladimir.infotecs.keyvaluedb.repository.mapper.KvPairRowMapper;
import org.vladimir.infotecs.keyvaluedb.repository.mapper.ValueWithExpirationTimeRowMapper;
import org.vladimir.infotecs.keyvaluedb.service.DbKVService;


import javax.sql.DataSource;
import java.sql.SQLException;

@ConditionalOnProperty(name = "useDb", havingValue = "true", matchIfMissing = false)
@Configuration
public class UseDbConfig {

    private final Integer databasePort;

    UseDbConfig(@Value("${database.port}") Integer databasePort) {
        this.databasePort = databasePort;
    }

    @Bean(name = "h2ServerBean", initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DatabaseServer() throws SQLException {
        return Server.createTcpServer("-tcp", "-ifNotExists", "-tcpPort", databasePort.toString());
    }

    @DependsOn("h2ServerBean")
    @Bean("hikariDataSource")
    public HikariDataSource dataSource() {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:h2:tcp://localhost:9090/mem:test");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("password");
        System.out.println(hikariConfig.getJdbcUrl());
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        String sql = "CREATE TABLE IF NOT EXISTS KEY_VALUE_TABLE (" +
                "\"KEY\" TEXT PRIMARY KEY, " +
                "\"VALUE\" TEXT NOT NULL, " +
                "EXPIRATION_TIME TIMESTAMP NOT NULL)";
        jdbcTemplate.execute(sql);
        return jdbcTemplate;
    }

    @Bean
    public RowMapper<ValueWithExpirationTime> valueWithExpirationTimeRowMapper() {
        return new ValueWithExpirationTimeRowMapper();
    }

    @Bean
    public RowMapper<KvPair> kvPairRowMapper() {
        return new KvPairRowMapper();
    }

    @Bean
    public DbKeyValueRepository dbKeyValueRepository(JdbcTemplate jdbcTemplate,
                                                     RowMapper<ValueWithExpirationTime> valueWithExpirationTimeRowMapper,
                                                     RowMapper<KvPair> kvPairRowMapper) {
        return new DbKeyValueRepository(jdbcTemplate, valueWithExpirationTimeRowMapper,kvPairRowMapper);
    }

    @Bean
    public DbKVService dbKvService(DbKeyValueRepository kvRepository, @Value("${defaultTTL:200}") Long defaultTTL) {
        return new DbKVService(kvRepository, defaultTTL);
    }
}
