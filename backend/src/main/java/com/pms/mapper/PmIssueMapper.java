package com.pms.mapper;

import com.pms.entity.PmIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmIssueMapper {
    List<PmIssue> selectPage(@Param("projectId") Long projectId, @Param("status") String status,
                             @Param("offset") int offset, @Param("limit") int limit);
    long countPage(@Param("projectId") Long projectId, @Param("status") String status);
    PmIssue selectById(@Param("id") Long id);
    String selectMaxIssueCode();
    int insert(PmIssue issue);
    int updateById(PmIssue issue);
    int deleteById(@Param("id") Long id);
}
