package org.example.thesisbuddy.service.impl.student;

import lombok.extern.slf4j.Slf4j;
import org.example.thesisbuddy.dao.StudentDao;
import org.example.thesisbuddy.dto.student.PasswordUpdateDTO;
import org.example.thesisbuddy.dto.student.StudentLoginDTO;
import org.example.thesisbuddy.entity.StudentAccount;
import org.example.thesisbuddy.service.student.StudentAuthService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StudentAuthServiceImpl implements StudentAuthService {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Result login(StudentLoginDTO loginDTO) {
        log.info("学生登录请求，学号: {}", loginDTO.getAccount());
        StudentAccount student = studentDao.selectByAccount(loginDTO.getAccount());
        
        if (student == null) {
            log.warn("学号不存在: {}", loginDTO.getAccount());
            return Result.error("学号不存在");
        }

        String dbPassword = student.getPasswordHash();
        
        if (dbPassword == null || dbPassword.isEmpty()) {
            log.warn("该账号未设置密码，学号: {}", loginDTO.getAccount());
            return Result.error("该账号未设置密码，请联系管理员");
        }
        
        if (!passwordEncoder.matches(loginDTO.getPassword(), dbPassword)) {
            log.warn("密码错误，学号: {}", loginDTO.getAccount());
            return Result.error("密码错误");
        }
        
        if (student.getStatus() == 0) {
            log.warn("账号已禁用，学号: {}", loginDTO.getAccount());
            return Result.error("账号已禁用");
        }
        
        student.setPasswordHash(null);
        log.info("登录成功，学号: {}", loginDTO.getAccount());
        return Result.success(student);
    }

    @Override
    public Result updatePassword(PasswordUpdateDTO passwordDTO) {
        StudentAccount student = studentDao.selectById(passwordDTO.getStudentId());
        if (student == null) return Result.error("学生不存在");
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), student.getPasswordHash())) return Result.error("旧密码错误");
        
        int rows = studentDao.updatePassword(passwordDTO.getStudentId(), passwordEncoder.encode(passwordDTO.getNewPassword()));
        return rows > 0 ? Result.success("密码修改成功") : Result.error("修改失败");
    }
}
