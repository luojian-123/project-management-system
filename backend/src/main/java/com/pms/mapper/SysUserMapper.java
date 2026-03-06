package com.pms.mapper;

import com.pms.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser selectByUsername(@Param("username") String username);
    SysUser selectById(@Param("id") Long id);
    List<SysUser> selectPage(@Param("keyword") String keyword, @Param("offset") long offset, @Param("limit") long limit);
    long countPage(@Param("keyword") String keyword);
    int insert(SysUser user);
    int updateById(SysUser user);
    int deleteById(@Param("id") Long id);
}
