package org.example.thesisbuddy.controller.student;

import org.example.thesisbuddy.dto.student.PasswordUpdateDTO;
import org.example.thesisbuddy.dto.student.StudentLoginDTO;
import org.example.thesisbuddy.service.student.StudentAuthService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/auth")
public class StudentAuthController {

    @Autowired
    private StudentAuthService studentAuthService;

    // 学生登录（POST /api/student/auth/login）
    @PostMapping("/login")
    public Result login(@RequestBody StudentLoginDTO loginDTO) {
        return studentAuthService.login(loginDTO);
    }

    // 修改密码（PUT /api/student/auth/password）
    @PutMapping("/password")
    public Result updatePassword(@RequestBody PasswordUpdateDTO passwordDTO) {
        return studentAuthService.updatePassword(passwordDTO);
    }
}
