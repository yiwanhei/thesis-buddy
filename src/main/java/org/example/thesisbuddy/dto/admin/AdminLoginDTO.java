package org.example.thesisbuddy.dto.admin;

import lombok.Data;

@Data//管理员登录
public class AdminLoginDTO {
    private String account;   // 账号（root）
    private String password;  // 密码
}
