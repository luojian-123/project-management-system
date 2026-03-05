package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmTodo;
import com.pms.mapper.PmTodoMapper;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmTodoService {

    private final PmTodoMapper todoMapper;

    public PmTodoService(PmTodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public PageResult<PmTodo> page(Long userId, int page, int size) {
        if (userId == null) {
            return new PageResult<>(0L, List.of());
        }
        int offset = (page - 1) * size;
        List<PmTodo> list = todoMapper.selectPage(userId, offset, size);
        long total = todoMapper.countPage(userId);
        return new PageResult<>(total, list);
    }

    public PmTodo getById(Long id) {
        return todoMapper.selectById(id);
    }

    public void save(PmTodo todo) {
        if (todo.getUserId() == null) todo.setUserId(WebConfig.getCurrentUserId());
        if (todo.getId() == null) {
            if (todo.getStatus() == null) todo.setStatus("pending");
            if (todo.getPriority() == null) todo.setPriority("medium");
            todoMapper.insert(todo);
        } else {
            todoMapper.updateById(todo);
        }
    }

    public void deleteById(Long id) {
        todoMapper.deleteById(id);
    }
}
