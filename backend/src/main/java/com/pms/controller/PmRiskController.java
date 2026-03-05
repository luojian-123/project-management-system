package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.entity.PmRisk;
import com.pms.service.PmRiskService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/risk")
public class PmRiskController {

    private final PmRiskService riskService;

    public PmRiskController(PmRiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/page")
    public Result<PageResult<PmRisk>> page(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(riskService.page(projectId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<PmRisk> getById(@PathVariable Long id) {
        return Result.ok(riskService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody PmRisk risk) {
        riskService.save(risk);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody PmRisk risk) {
        riskService.save(risk);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        riskService.deleteById(id);
        return Result.ok();
    }
}
