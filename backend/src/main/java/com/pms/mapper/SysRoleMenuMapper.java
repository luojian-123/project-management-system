package com.pms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper {
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    int deleteByRoleId(@Param("roleId") Long roleId);
    int deleteByMenuId(@Param("menuId") Long menuId);
    int insert(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}

