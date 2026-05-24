package org.example.thesisbuddy.service.admin;

import org.example.thesisbuddy.dto.admin.AdminLoginDTO;
import org.example.thesisbuddy.common.Result;

public interface AdminAuthService {
    // 管理员登录
    Result login(AdminLoginDTO loginDTO);
}
