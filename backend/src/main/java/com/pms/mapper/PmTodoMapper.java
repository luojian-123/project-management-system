package com.pms.mapper;

import com.pms.entity.PmTodo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmTodoMapper {
    List<PmTodo> selectPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    long countPage(@Param("userId") Long userId);
    PmTodo selectById(@Param("id") Long id);
    int insert(PmTodo todo);
    int updateById(PmTodo todo);
    int deleteById(@Param("id") Long id);
}
