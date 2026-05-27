package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dto.admin.BatchImportDTO;
import org.example.thesisbuddy.dto.admin.UserAddDTO;
import org.example.thesisbuddy.dto.admin.UserStatusDTO;
import org.example.thesisbuddy.service.admin.AdminUserManageService;
import org.example.thesisbuddy.dao.ApplicationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserManageController {

    @Autowired
    private AdminUserManageService adminUserManageService;

    // 新增学生/教师账号
    @PostMapping("/add")
    public Result addUser(@RequestBody UserAddDTO userDTO) {
        return adminUserManageService.addUser(userDTO);
    }

    // 禁用/启用账号
    @PutMapping("/status")
    public Result updateUserStatus(@RequestBody UserStatusDTO statusDTO) {
        return adminUserManageService.updateUserStatus(statusDTO);
    }

    // 批量导入学生
    @PostMapping("/batch-import")
    public Result batchImportStudents(@RequestBody BatchImportDTO importDTO) {
        return adminUserManageService.batchImportStudents(importDTO);
    }

    // 查看全校用户统计
    @GetMapping("/stat")
    public Result getUserStat() {
        return adminUserManageService.getUserStat();
    }

    // 导出用户数据
    @GetMapping("/export")
    public Result exportUsers(@RequestParam String type) {
        return adminUserManageService.exportUsers(type);
    }

    // 分页查询用户列表
    @GetMapping("/list")
    public Result listUsers(@RequestParam String type,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size) {
        return adminUserManageService.listUsers(type, keyword, page, size);
    }

    // 获取班级列表
    @GetMapping("/classes")
    public Result listClasses() {
        return adminUserManageService.listClasses();
    }
    
    // 获取仪表盘统计数据
    @GetMapping("/dashboard")
    public Result dashboard() {
        return adminUserManageService.getDashboardStats();
    }
}
