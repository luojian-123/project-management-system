package com.pms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleDeptMapper {
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);
    List<Long> selectRoleIdsByDeptId(@Param("deptId") Long deptId);
    int deleteByRoleId(@Param("roleId") Long roleId);
    int insert(@Param("roleId") Long roleId, @Param("deptId") Long deptId);
}
