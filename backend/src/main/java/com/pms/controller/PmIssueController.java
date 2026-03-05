package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.entity.PmIssue;
import com.pms.service.PmIssueService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/issue")
public class PmIssueController {

    private final PmIssueService issueService;

    public PmIssueController(PmIssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/page")
    public Result<PageResult<PmIssue>> page(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(issueService.page(projectId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<PmIssue> getById(@PathVariable Long id) {
        return Result.ok(issueService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody PmIssue issue) {
        issueService.save(issue);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody PmIssue issue) {
        issueService.save(issue);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        issueService.deleteById(id);
        return Result.ok();
    }
}
