package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.entity.PmProject;
import com.pms.service.PmProjectService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
public class PmProjectController {

    private final PmProjectService projectService;

    public PmProjectController(PmProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/page")
    public Result<PageResult<PmProject>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(projectService.page(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<PmProject> getById(@PathVariable Long id) {
        return Result.ok(projectService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody PmProject project) {
        projectService.save(project);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody PmProject project) {
        projectService.save(project);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.deleteById(id);
        return Result.ok();
    }
}
