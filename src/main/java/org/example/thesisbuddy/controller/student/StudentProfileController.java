package org.example.thesisbuddy.controller.student;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.service.student.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
public class StudentProfileController {

    @Autowired
    private StudentProfileService studentProfileService;

    // 查看个人信息
    @GetMapping("/my")
    public Result getMyProfile(@RequestParam int studentId) {
        return studentProfileService.getMyProfile(studentId);
    }

    // 修改手机号
    @PutMapping("/phone")
    public Result updatePhone(@RequestBody Object phoneDTO) {
        // TODO: 实现修改逻辑
        return Result.success();
    }

    // 查看我的选题记录
    @GetMapping("/application/my")
    public Result getMyApplication(@RequestParam int studentId) {
        // TODO: 实现查询逻辑
        return Result.success();
    }
    
    // 获取本班级的教师列表（用于指导教师选择）
    @GetMapping("/teachers")
    public Result getClassTeachers(@RequestParam int studentId) {
        return studentProfileService.getClassTeachers(studentId);
    }
}
