package com.pms.mapper;

import com.pms.entity.PmTaskChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmTaskChangeMapper {
    List<PmTaskChange> selectByTaskId(@Param("taskId") Long taskId);
    int insert(PmTaskChange change);
}
