package com.pms.controller;

import com.pms.common.OrgTreeNode;
import com.pms.common.Result;
import com.pms.entity.PmCompany;
import com.pms.entity.PmDept;
import com.pms.service.OrgTreeService;
import com.pms.service.PmCompanyService;
import com.pms.service.PmDeptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrgController {

    private final PmCompanyService companyService;
    private final PmDeptService deptService;
    private final OrgTreeService orgTreeService;

    public OrgController(PmCompanyService companyService, PmDeptService deptService, OrgTreeService orgTreeService) {
        this.companyService = companyService;
        this.deptService = deptService;
        this.orgTreeService = orgTreeService;
    }

    @GetMapping("/tree")
    public Result<List<OrgTreeNode>> tree() {
        return Result.ok(orgTreeService.buildTree());
    }

    @GetMapping("/company/list")
    public Result<List<PmCompany>> companyList() {
        return Result.ok(companyService.list());
    }

    @GetMapping("/company/{id}")
    public Result<PmCompany> companyGet(@PathVariable Long id) {
        return Result.ok(companyService.getById(id));
    }

    @PostMapping("/company")
    public Result<Void> companySave(@RequestBody PmCompany company) {
        companyService.save(company);
        return Result.ok();
    }

    @PutMapping("/company")
    public Result<Void> companyUpdate(@RequestBody PmCompany company) {
        companyService.save(company);
        return Result.ok();
    }

    @DeleteMapping("/company/{id}")
    public Result<Void> companyDelete(@PathVariable Long id) {
        companyService.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/dept/list")
    public Result<List<PmDept>> deptList(@RequestParam Long companyId) {
        return Result.ok(deptService.listByCompanyId(companyId));
    }

    @GetMapping("/dept/{id}")
    public Result<PmDept> deptGet(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @PostMapping("/dept")
    public Result<Void> deptSave(@RequestBody PmDept dept) {
        deptService.save(dept);
        return Result.ok();
    }

    @PutMapping("/dept")
    public Result<Void> deptUpdate(@RequestBody PmDept dept) {
        deptService.save(dept);
        return Result.ok();
    }

    @DeleteMapping("/dept/{id}")
    public Result<Void> deptDelete(@PathVariable Long id) {
        deptService.deleteById(id);
        return Result.ok();
    }
}

