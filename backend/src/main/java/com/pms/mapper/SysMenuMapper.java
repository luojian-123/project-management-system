package com.pms.mapper;

import com.pms.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper {
    List<SysMenu> selectByUserId(@Param("userId") Long userId);
    List<SysMenu> selectChildren(@Param("parentId") Long parentId);
    List<SysMenu> selectAll();
    SysMenu selectById(@Param("id") Long id);
    int insert(SysMenu menu);
    int updateById(SysMenu menu);
    int deleteById(@Param("id") Long id);
}
