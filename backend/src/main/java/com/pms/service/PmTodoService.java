package com.pms.service;

import com.pms.common.PageResult;
import com.pms.entity.PmTodo;
import com.pms.repository.PmTodoRepository;
import com.pms.config.WebConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PmTodoService {

    private final PmTodoRepository todoRepository;

    public PmTodoService(PmTodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public PageResult<PmTodo> page(Long userId, int page, int size) {
        if (userId == null) {
            return new PageResult<>(0L, List.of());
        }
        int offset = (page - 1) * size;
        List<PmTodo> list = todoRepository.selectPage(userId, offset, size);
        long total = todoRepository.countPage(userId);
        return new PageResult<>(total, list);
    }

    public PmTodo getById(Long id) {
        return todoRepository.selectById(id);
    }

    public void save(PmTodo todo) {
        if (todo.getUserId() == null) todo.setUserId(WebConfig.getCurrentUserId());
        if (todo.getId() == null) {
            if (todo.getStatus() == null) todo.setStatus("pending");
            if (todo.getPriority() == null) todo.setPriority("medium");
            todoRepository.insert(todo);
        } else {
            todoRepository.updateById(todo);
        }
    }

    public void deleteById(Long id) {
        todoRepository.deleteById(id);
    }
}
