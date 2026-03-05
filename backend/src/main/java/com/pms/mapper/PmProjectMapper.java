package com.pms.mapper;

import com.pms.entity.PmProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmProjectMapper {
    List<PmProject> selectPage(@Param("keyword") String keyword, @Param("status") String status,
                              @Param("offset") int offset, @Param("limit") int limit);
    long countPage(@Param("keyword") String keyword, @Param("status") String status);
    PmProject selectById(@Param("id") Long id);
    int insert(PmProject project);
    int updateById(PmProject project);
    int deleteById(@Param("id") Long id);
}
