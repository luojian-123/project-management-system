package com.pms.mapper;

import com.pms.entity.PmRisk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmRiskMapper {
    List<PmRisk> selectPage(@Param("projectId") Long projectId, @Param("status") String status,
                            @Param("offset") int offset, @Param("limit") int limit);
    long countPage(@Param("projectId") Long projectId, @Param("status") String status);
    PmRisk selectById(@Param("id") Long id);
    String selectMaxRiskCode();
    int insert(PmRisk risk);
    int updateById(PmRisk risk);
    int deleteById(@Param("id") Long id);
}
