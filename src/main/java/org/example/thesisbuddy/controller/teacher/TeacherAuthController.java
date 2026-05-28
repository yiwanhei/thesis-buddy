package org.example.thesisbuddy.controller.teacher;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dto.teacher.TeacherLoginDTO;
import org.example.thesisbuddy.service.teacher.TeacherAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/auth")
public class TeacherAuthController {

    @Autowired
    private TeacherAuthService teacherAuthService;

    // 教师登录
    @PostMapping("/login")
    public Result login(@RequestBody TeacherLoginDTO loginDTO) {
        return teacherAuthService.login(loginDTO);
    }
}
