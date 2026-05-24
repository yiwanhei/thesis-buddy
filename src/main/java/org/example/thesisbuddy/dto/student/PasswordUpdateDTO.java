package org.example.thesisbuddy.dto.student;

import lombok.Data;

@Data//修改密码
public class PasswordUpdateDTO {
    private int studentId;      // 学生 ID
    private String oldPassword; // 旧密码
    private String newPassword; // 新密码
}
