package org.example.thesisbuddy.service.student;

import org.example.thesisbuddy.dto.student.PasswordUpdateDTO;
import org.example.thesisbuddy.dto.student.StudentLoginDTO;
import org.example.thesisbuddy.common.Result;

public interface StudentAuthService {
    // 学生登录
    Result login(StudentLoginDTO loginDTO);

    // 修改密码
    Result updatePassword(PasswordUpdateDTO passwordDTO);
}
