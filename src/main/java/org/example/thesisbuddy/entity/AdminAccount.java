package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminAccount {
    private Integer adminId;
    private String account;           // 账号（root）
    private String passwordHash;      // 密码
    private String realName;          // 姓名
    private Integer status;           // 状态
    private LocalDateTime createTime;
}
