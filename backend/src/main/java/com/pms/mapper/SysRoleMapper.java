package com.pms.mapper;

import com.pms.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    List<SysRole> selectList();
    List<SysRole> selectByDeptId(@Param("deptId") Long deptId);
    SysRole selectById(@Param("id") Long id);
    int insert(SysRole role);
    int updateById(SysRole role);
    int deleteById(@Param("id") Long id);
}

