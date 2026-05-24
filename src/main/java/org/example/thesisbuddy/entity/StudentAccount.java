package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data//学生账号实体
public class StudentAccount {
    private Integer studentId;
    private String account;        // 学号
    private String passwordHash;   // 密码
    private String realName;       // 姓名
    private String gender;         // 性别
    private String phone;          // 手机号
    private Integer classId;       // 班级 ID
    private Integer status;        // 状态：1 正常 0 禁用
    private LocalDateTime createTime;
}
