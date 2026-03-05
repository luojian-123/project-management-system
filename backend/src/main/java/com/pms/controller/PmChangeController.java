package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.entity.PmChange;
import com.pms.service.PmChangeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/change")
public class PmChangeController {

    private final PmChangeService changeService;

    public PmChangeController(PmChangeService changeService) {
        this.changeService = changeService;
    }

    @GetMapping("/page")
    public Result<PageResult<PmChange>> page(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(changeService.page(projectId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<PmChange> getById(@PathVariable Long id) {
        return Result.ok(changeService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody PmChange change) {
        changeService.save(change);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody PmChange change) {
        changeService.save(change);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        changeService.deleteById(id);
        return Result.ok();
    }
}
