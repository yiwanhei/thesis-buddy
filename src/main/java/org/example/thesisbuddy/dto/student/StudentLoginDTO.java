package org.example.thesisbuddy.dto.student;

import lombok.Data;

@Data//学生登录
public class StudentLoginDTO {
    private String account;    // 学号
    private String password;   // 密码
}
