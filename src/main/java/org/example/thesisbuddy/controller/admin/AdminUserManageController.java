package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserManageController {

    // 新增学生/教师账号
    @PostMapping("/add")
    public Result addUser(@RequestBody Object userDTO) {
        // TODO: 实现添加逻辑
        return Result.success();
    }

    // 禁用/启用账号
    @PutMapping("/status")
    public Result updateUserStatus(@RequestBody Object statusDTO) {
        // TODO: 实现状态修改逻辑
        return Result.success();
    }

    // 批量导入学生
    @PostMapping("/batch-import")
    public Result batchImportStudents(@RequestBody Object importDTO) {
        // TODO: 实现批量导入逻辑
        return Result.success();
    }

    // 查看全校用户统计
    @GetMapping("/stat")
    public Result getUserStat() {
        // TODO: 实现统计逻辑
        return Result.success();
    }

    // 导出用户数据
    @GetMapping("/export")
    public Result exportUsers(@RequestParam String type) {
        // TODO: type: student/teacher
        return Result.success();
    }
}
