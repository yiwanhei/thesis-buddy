package org.example.thesisbuddy.service.admin;

import org.example.thesisbuddy.dto.admin.UserAddDTO;
import org.example.thesisbuddy.dto.admin.UserStatusDTO;
import org.example.thesisbuddy.dto.admin.BatchImportDTO;
import org.example.thesisbuddy.common.Result;

public interface AdminUserManageService {
    // 新增用户
    Result addUser(UserAddDTO userDTO);

    // 修改用户状态
    Result updateUserStatus(UserStatusDTO statusDTO);

    // 批量导入学生
    Result batchImportStudents(BatchImportDTO importDTO);

    // 查看全校用户统计
    Result getUserStat();

    // 导出用户数据
    Result exportUsers(String type);
    
    // 分页查询用户列表
    Result listUsers(String type, String keyword, int page, int size);
    
    // 查询班级列表
    Result listClasses();
    
    // 获取仪表盘统计数据
    Result getDashboardStats();
}
