package com.pms.mapper;

import com.pms.entity.PmDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmDeptMapper {
    List<PmDept> selectByCompanyId(@Param("companyId") Long companyId);
    PmDept selectById(@Param("id") Long id);
    int insert(PmDept dept);
    int updateById(PmDept dept);
    int deleteById(@Param("id") Long id);
}

