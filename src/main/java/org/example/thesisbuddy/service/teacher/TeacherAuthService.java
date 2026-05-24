package org.example.thesisbuddy.service.teacher;

import org.example.thesisbuddy.dto.teacher.TeacherLoginDTO;
import org.example.thesisbuddy.common.Result;

public interface TeacherAuthService {
    // 教师登录
    Result login(TeacherLoginDTO loginDTO);
}
