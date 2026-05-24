package org.example.thesisbuddy.service.impl.teacher;

import org.example.thesisbuddy.dao.TeacherDao;
import org.example.thesisbuddy.dto.teacher.TeacherLoginDTO;
import org.example.thesisbuddy.entity.TeacherAccount;
import org.example.thesisbuddy.service.teacher.TeacherAuthService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherAuthServiceImpl implements TeacherAuthService {

    @Autowired
    private TeacherDao teacherDao;

    @Override
    public Result login(TeacherLoginDTO loginDTO) {
        TeacherAccount teacher = teacherDao.selectByAccount(loginDTO.getAccount());
        if (teacher == null) return Result.error("工号不存在");
        if (!teacher.getPasswordHash().equals(loginDTO.getPassword())) return Result.error("密码错误");
        teacher.setPasswordHash(null);
        return Result.success(teacher);
    }
}
