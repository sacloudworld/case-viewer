package com.example.case_viewer.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CaseRowMapper implements RowMapper<CaseResponse> {

    @Override
    public CaseResponse mapRow(ResultSet rs, int rowNum)
            throws SQLException {

        return new CaseResponse(
       
            rs.getLong("CASE_ID"),
            rs.getString("STATUS"),
            rs.getTimestamp("UPDATED_AT") != null
                        ? rs.getTimestamp("UPDATED_AT").toLocalDateTime()
                        : null
        );
    }
}