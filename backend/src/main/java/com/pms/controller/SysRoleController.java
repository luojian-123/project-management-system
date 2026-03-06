package com.pms.controller;

import com.pms.common.Result;
import com.pms.entity.SysRole;
import com.pms.service.SysRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/role")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.list());
    }

    @GetMapping("/listByDept")
    public Result<List<SysRole>> listByDept(@RequestParam Long deptId) {
        return Result.ok(roleService.listByDeptId(deptId));
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/{id}/menus")
    public Result<List<Long>> menuIds(@PathVariable Long id) {
        return Result.ok(roleService.getMenuIdsByRoleId(id));
    }

    @PostMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.setMenus(id, menuIds);
        return Result.ok();
    }
}

