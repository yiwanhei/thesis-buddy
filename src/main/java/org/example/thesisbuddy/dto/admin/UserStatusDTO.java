package org.example.thesisbuddy.dto.admin;

import lombok.Data;

@Data//修改用户状态
public class UserStatusDTO {
    private String userType;  // 用户类型：student/teacher
    private int userId;       // 用户 ID
    private int status;       // 状态：1 正常 0 禁用
}
