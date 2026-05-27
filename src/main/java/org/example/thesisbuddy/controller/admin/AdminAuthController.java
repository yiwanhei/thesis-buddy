package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dto.admin.AdminLoginDTO;
import org.example.thesisbuddy.service.admin.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    // 管理员登录
    @PostMapping("/login")
    public Result login(@RequestBody AdminLoginDTO loginDTO) {
        return adminAuthService.login(loginDTO);
    }
}
