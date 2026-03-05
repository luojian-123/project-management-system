package com.pms.controller;

import com.pms.common.Result;
import com.pms.config.WebConfig;
import com.pms.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.fail("用户名和密码不能为空");
        }
        return Result.ok(authService.login(username, password));
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = WebConfig.getCurrentUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.ok(authService.getInfo(userId));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String realName = body.get("realName");
        if (username == null || password == null) return Result.fail("用户名和密码不能为空");
        authService.register(username, password, realName);
        return Result.ok();
    }
}
