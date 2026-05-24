package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    // 管理员登录
    @PostMapping("/login")
    public Result login(@RequestBody Object loginDTO) {
        // TODO: 实现登录逻辑
        return Result.success();
    }
}
