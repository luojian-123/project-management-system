package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.entity.PmCost;
import com.pms.service.PmCostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cost")
public class PmCostController {

    private final PmCostService costService;

    public PmCostController(PmCostService costService) {
        this.costService = costService;
    }

    @GetMapping("/page")
    public Result<PageResult<PmCost>> page(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(costService.page(projectId, page, size));
    }

    @GetMapping("/{id}")
    public Result<PmCost> getById(@PathVariable Long id) {
        return Result.ok(costService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody PmCost cost) {
        costService.save(cost);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody PmCost cost) {
        costService.save(cost);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        costService.deleteById(id);
        return Result.ok();
    }
}
