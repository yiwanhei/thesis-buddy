package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherAccount {
    private Integer teacherId;
    private String account;           // 工号
    private String passwordHash;      // 密码
    private String realName;          // 姓名
    private String phone;             // 手机号
    private Integer campusId;         // 所属校区
    private String title;             // 职称
    private Integer status;           // 状态 1 正常 0 禁用
    private LocalDateTime createTime;
}
