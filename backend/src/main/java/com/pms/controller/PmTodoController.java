package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.config.WebConfig;
import com.pms.entity.PmTodo;
import com.pms.service.PmTodoService;

import java.util.Collections;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/todo")
public class PmTodoController {

    private final PmTodoService todoService;

    public PmTodoController(PmTodoService todoService) {
        this.todoService = todoService;
    }

    /** 个人待办分页：仅返回当前登录用户作为任务责任人(user_id)的待办 */
    @GetMapping("/page")
    public Result<PageResult<PmTodo>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = WebConfig.getCurrentUserId();
        if (currentUserId == null) {
            return Result.ok(new PageResult<>(0L, Collections.emptyList()));
        }
        return Result.ok(todoService.page(currentUserId, page, size));
    }

    @GetMapping("/{id}")
    public Result<PmTodo> getById(@PathVariable Long id) {
        PmTodo todo = todoService.getById(id);
        if (todo == null) return Result.ok(null);
        Long currentUserId = WebConfig.getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(todo.getUserId())) {
            return Result.ok(null);
        }
        return Result.ok(todo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody PmTodo todo) {
        todoService.save(todo);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody PmTodo todo) {
        if (todo.getId() != null) {
            PmTodo existing = todoService.getById(todo.getId());
            if (existing != null) {
                Long currentUserId = WebConfig.getCurrentUserId();
                if (currentUserId == null || !currentUserId.equals(existing.getUserId())) {
                    return Result.ok();
                }
            }
        }
        todoService.save(todo);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        PmTodo todo = todoService.getById(id);
        if (todo != null) {
            Long currentUserId = WebConfig.getCurrentUserId();
            if (currentUserId != null && currentUserId.equals(todo.getUserId())) {
                todoService.deleteById(id);
            }
        }
        return Result.ok();
    }
}
