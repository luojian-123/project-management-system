package com.pms.mapper;

import com.pms.entity.PmTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmTaskMapper {
    List<PmTask> selectPageByAssignee(@Param("assigneeId") Long assigneeId, @Param("offset") int offset, @Param("limit") int limit);
    long countPageByAssignee(@Param("assigneeId") Long assigneeId);
    PmTask selectById(@Param("id") Long id);
    PmTask selectByIdWithNames(@Param("id") Long id);
    List<PmTask> selectByProjectId(@Param("projectId") Long projectId);
    int insert(PmTask task);
    int updateById(PmTask task);
    int deleteById(@Param("id") Long id);
}
