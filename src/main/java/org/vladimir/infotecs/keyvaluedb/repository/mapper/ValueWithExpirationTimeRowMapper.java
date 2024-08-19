package org.vladimir.infotecs.keyvaluedb.repository.mapper;

import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ValueWithExpirationTimeRowMapper implements RowMapper<ValueWithExpirationTime> {
    @Override
    public ValueWithExpirationTime mapRow(ResultSet rs, int rowNum) throws SQLException {
        String value = rs.getString("VALUE");
        Long expirationTime = rs.getTimestamp("EXPIRATION_TIME").getTime() / 1000; // преобразуем миллисекунды в секунды
        return new ValueWithExpirationTime(value, expirationTime);
    }
}