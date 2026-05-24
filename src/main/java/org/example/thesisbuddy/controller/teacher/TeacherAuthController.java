package org.example.thesisbuddy.controller.teacher;

import org.example.thesisbuddy.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/auth")
public class TeacherAuthController {

    // 教师登录
    @PostMapping("/login")
    public Result login(@RequestBody Object loginDTO) {
        // TODO: 实现登录逻辑
        return Result.success();
    }
}
