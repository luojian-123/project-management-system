package com.pms.mapper;

import com.pms.entity.PmChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmChangeMapper {
    List<PmChange> selectPage(@Param("projectId") Long projectId, @Param("status") String status,
                             @Param("offset") int offset, @Param("limit") int limit);
    long countPage(@Param("projectId") Long projectId, @Param("status") String status);
    PmChange selectById(@Param("id") Long id);
    String selectMaxChangeNo();
    int insert(PmChange change);
    int updateById(PmChange change);
    int deleteById(@Param("id") Long id);
}
