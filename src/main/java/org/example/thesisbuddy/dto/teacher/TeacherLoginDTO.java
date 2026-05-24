package org.example.thesisbuddy.dto.teacher;

import lombok.Data;

@Data//教师登录
public class TeacherLoginDTO {
    private String account;   // 工号
    private String password;  // 密码
}
