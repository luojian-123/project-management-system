package com.pms.controller;

import com.pms.common.Result;
import com.pms.entity.SysMenu;
import com.pms.service.SysMenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.ok(menuService.tree());
    }

    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.ok(menuService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.deleteById(id);
        return Result.ok();
    }
}

