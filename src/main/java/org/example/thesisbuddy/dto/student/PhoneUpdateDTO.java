package org.example.thesisbuddy.dto.student;

import lombok.Data;

@Data//修改手机号
public class PhoneUpdateDTO {
    private int studentId;  // 学生 ID
    private String phone;   // 新手机号
}
