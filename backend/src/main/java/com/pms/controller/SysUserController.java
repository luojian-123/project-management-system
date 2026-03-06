package com.pms.controller;

import com.pms.common.PageResult;
import com.pms.common.Result;
import com.pms.entity.SysUser;
import com.pms.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return Result.ok(userService.page(keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysUser user) {
        userService.save(user);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.save(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.ok();
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> roleIds(@PathVariable Long id) {
        return Result.ok(userService.getRoleIdsByUserId(id));
    }

    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.setRoles(id, roleIds);
        return Result.ok();
    }
}

