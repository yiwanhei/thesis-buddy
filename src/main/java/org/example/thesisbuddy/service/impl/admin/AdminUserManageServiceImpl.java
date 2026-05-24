package org.example.thesisbuddy.service.impl.admin;

import org.example.thesisbuddy.dao.StudentDao;
import org.example.thesisbuddy.dao.TeacherDao;
import org.example.thesisbuddy.dto.admin.UserAddDTO;
import org.example.thesisbuddy.dto.admin.UserStatusDTO;
import org.example.thesisbuddy.dto.admin.BatchImportDTO;
import org.example.thesisbuddy.entity.StudentAccount;
import org.example.thesisbuddy.entity.TeacherAccount;
import org.example.thesisbuddy.service.admin.AdminUserManageService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AdminUserManageServiceImpl implements AdminUserManageService {

    @Autowired
    private StudentDao studentDao;
    
    @Autowired
    private TeacherDao teacherDao;

    @Override
    public Result addUser(UserAddDTO userDTO) {
        if ("student".equals(userDTO.getUserType())) {
            StudentAccount student = new StudentAccount();
            student.setAccount(userDTO.getAccount());
            student.setPasswordHash("123456");
            student.setRealName(userDTO.getRealName());
            student.setGender(userDTO.getGender());
            student.setClassId(userDTO.getClassId());
            student.setStatus(1);
            
            int rows = studentDao.insert(student);
            return rows > 0 ? Result.success("学生账号创建成功") : Result.error("创建失败");
        } else if ("teacher".equals(userDTO.getUserType())) {
            TeacherAccount teacher = new TeacherAccount();
            teacher.setAccount(userDTO.getAccount());
            teacher.setPasswordHash("123456");
            teacher.setRealName(userDTO.getRealName());
            teacher.setCampusId(userDTO.getCampusId());
            teacher.setTitle(userDTO.getTitle());
            teacher.setStatus(1);
            
            int rows = teacherDao.insert(teacher);
            return rows > 0 ? Result.success("教师账号创建成功") : Result.error("创建失败");
        }
        
        return Result.error("用户类型错误");
    }

    @Override
    public Result updateUserStatus(UserStatusDTO statusDTO) {
        int rows = 0;
        if ("student".equals(statusDTO.getUserType())) {
            rows = studentDao.updateStatus(statusDTO.getUserId(), statusDTO.getStatus());
        } else if ("teacher".equals(statusDTO.getUserType())) {
            rows = teacherDao.updateStatus(statusDTO.getUserId(), statusDTO.getStatus());
        }
        
        return rows > 0 ? Result.success("状态修改成功") : Result.error("修改失败");
    }

    @Override
    public Result batchImportStudents(BatchImportDTO importDTO) {
        int successCount = 0;
        int failCount = 0;
        
        for (BatchImportDTO.StudentImportItem item : importDTO.getStudents()) {
            StudentAccount student = new StudentAccount();
            student.setAccount(item.getAccount());
            student.setPasswordHash("123456");
            student.setRealName(item.getRealName());
            student.setGender(item.getGender());
            student.setClassId(item.getClassId());
            student.setStatus(1);
            
            try {
                studentDao.insert(student);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        data.put("failCount", failCount);
        return Result.success(data);
    }

    @Override
    public Result getUserStat() {
        Map<String, Object> data = new HashMap<>();
        data.put("studentCount", studentDao.countAll());
        data.put("teacherCount", teacherDao.countAll());
        return Result.success(data);
    }

    @Override
    public Result exportUsers(String type) {
        if ("student".equals(type)) {
            return Result.success(studentDao.selectAll());
        } else if ("teacher".equals(type)) {
            return Result.success(teacherDao.selectAll());
        }
        return Result.error("类型错误");
    }
}
