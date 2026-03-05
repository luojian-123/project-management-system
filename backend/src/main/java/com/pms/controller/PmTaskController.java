package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.config.WebConfig;
import com.pms.entity.PmTask;
import com.pms.entity.PmTaskChange;
import com.pms.service.PmTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/task")
public class PmTaskController {

    private final PmTaskService taskService;

    public PmTaskController(PmTaskService taskService) {
        this.taskService = taskService;
    }

    /** 按责任人分页查询任务：仅返回当前登录用户作为责任人的任务 */
    @GetMapping("/by-assignee")
    public Result<PageResult<PmTask>> pageByAssignee(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = WebConfig.getCurrentUserId();
        if (currentUserId == null) {
            return Result.ok(new PageResult<>(0L, Collections.emptyList()));
        }
        return Result.ok(taskService.pageByAssignee(currentUserId, page, size));
    }

    /** 按项目查询任务列表，tree=true 时返回树形（含 children） */
    @GetMapping("/project/{projectId}")
    public Result<List<PmTask>> listByProject(
            @PathVariable Long projectId,
            @RequestParam(value = "tree", defaultValue = "false") boolean tree) {
        return Result.ok(taskService.listByProject(projectId, tree));
    }

    /** 任务详情（含项目名、负责人名） */
    @GetMapping("/{id}")
    public Result<PmTask> getById(@PathVariable Long id) {
        return Result.ok(taskService.getDetailById(id));
    }

    /** 任务变更记录 */
    @GetMapping("/{id}/changes")
    public Result<List<PmTaskChange>> getChanges(@PathVariable Long id) {
        return Result.ok(taskService.listChangesByTaskId(id));
    }
}
