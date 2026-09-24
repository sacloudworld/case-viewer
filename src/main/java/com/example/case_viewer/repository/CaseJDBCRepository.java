package com.example.case_viewer.repository;

import oracle.jdbc.OracleTypes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.example.case_viewer.mapper.CaseResponse;
import com.example.case_viewer.mapper.CaseRowMapper;

import java.util.List;
import java.util.Map;

@Repository
public class CaseJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public CaseJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CaseResponse> getAllCases() {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GET_ALL_CASES")
                .declareParameters(
                        new SqlOutParameter(
                                "P_CASES",
                                OracleTypes.CURSOR,
                                new CaseRowMapper()
                        )
                );

        Map<String, Object> result = jdbcCall.execute();

        System.out.println("Stored procedure result1 = " + result);
        System.out.println("Result keys = " + result.keySet());

        List<?> rows = (List<?>) result.get("P_CASES");

        return rows.stream()
                .map(CaseResponse.class::cast)
                .toList();
    }

    public CaseResponse getJDBCCase(Long caseId) {

        String sql =  """
                SELECT CASE_ID,
                       STATUS,
                       UPDATED_AT
                FROM CASES
                WHERE CASE_ID = ?
                """;

            return jdbcTemplate.queryForObject(sql,new CaseRowMapper(),caseId);

           
    }
}