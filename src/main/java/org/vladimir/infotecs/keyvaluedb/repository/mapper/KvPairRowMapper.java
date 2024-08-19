package org.vladimir.infotecs.keyvaluedb.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.vladimir.infotecs.keyvaluedb.model.KvPair;
import org.vladimir.infotecs.keyvaluedb.model.ValueWithExpirationTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class KvPairRowMapper implements RowMapper<KvPair> {
    @Override
    public KvPair mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new KvPair(rs.getString("KEY"),
                new ValueWithExpirationTime(
                        rs.getString("VALUE"),
                        rs.getTimestamp("EXPIRATION_TIME").getTime()));
    }
}
