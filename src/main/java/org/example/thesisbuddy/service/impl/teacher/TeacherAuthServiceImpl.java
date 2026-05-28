package org.example.thesisbuddy.service.impl.teacher;

import org.example.thesisbuddy.common.JwtUtil;
import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dao.TeacherDao;
import org.example.thesisbuddy.dto.teacher.TeacherLoginDTO;
import org.example.thesisbuddy.entity.TeacherAccount;
import org.example.thesisbuddy.service.teacher.TeacherAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TeacherAuthServiceImpl implements TeacherAuthService {

    @Autowired
    private TeacherDao teacherDao;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Result login(TeacherLoginDTO loginDTO) {
        TeacherAccount teacher = teacherDao.selectByAccount(loginDTO.getAccount());
        if (teacher == null) return Result.error("账号不存在");
        if (teacher.getStatus() == 0) return Result.error("账号已被禁用");
        if (!passwordEncoder.matches(loginDTO.getPassword(), teacher.getPasswordHash())) return Result.error("密码错误");

        // 生成教师JWT Token
        String token = jwtUtil.generateTeacherToken(teacher.getTeacherId(), teacher.getAccount());
        teacher.setPasswordHash(null);

        Map<String, Object> data = new HashMap<>();
        data.put("teacher", teacher);
        data.put("token", token);
        return Result.success(data);
    }
}
