package org.example.thesisbuddy.dto.admin;

import lombok.Data;

@Data//新增用户
public class UserAddDTO {
    private String userType;   // 用户类型：student/teacher
    private String account;    // 账号（学号/工号）
    private String realName;   // 姓名
    private String gender;     // 性别
    private Integer classId;   // 班级 ID（学生必填）
    private Integer campusId;  // 校区 ID（教师选填）
    private String title;      // 职称（教师选填）
}
