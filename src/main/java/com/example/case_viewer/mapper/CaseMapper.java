package com.example.case_viewer.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.example.case_viewer.mybatis.entity.*;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CaseMapper {

    Case findCase(
        @Param("caseId") Long caseId,
        @Param("owner") String owner
    );
}