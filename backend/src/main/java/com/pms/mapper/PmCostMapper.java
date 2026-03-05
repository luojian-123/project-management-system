package com.pms.mapper;

import com.pms.entity.PmCost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmCostMapper {
    List<PmCost> selectPage(@Param("projectId") Long projectId, @Param("offset") int offset, @Param("limit") int limit);
    long countPage(@Param("projectId") Long projectId);
    PmCost selectById(@Param("id") Long id);
    int insert(PmCost cost);
    int updateById(PmCost cost);
    int deleteById(@Param("id") Long id);
}
