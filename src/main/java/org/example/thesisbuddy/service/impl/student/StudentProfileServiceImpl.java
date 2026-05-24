package org.example.thesisbuddy.service.impl.student;

import org.example.thesisbuddy.dao.StudentDao;
import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.ClassDao;
import org.example.thesisbuddy.dao.TeacherDao;
import org.example.thesisbuddy.dto.student.PhoneUpdateDTO;
import org.example.thesisbuddy.entity.StudentAccount;
import org.example.thesisbuddy.entity.ClassInfo;
import org.example.thesisbuddy.entity.TeacherAccount;
import org.example.thesisbuddy.service.student.StudentProfileService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    @Autowired
    private StudentDao studentDao;
    
    @Autowired
    private ClassDao classDao;
    
    @Autowired
    private ApplicationDao applicationDao;
    
    @Autowired
    private TeacherDao teacherDao;


    @Override
    public Result getMyProfile(int studentId) {
        StudentAccount student = studentDao.selectById(studentId);
        if (student == null) return Result.error("学生不存在");
        
        ClassInfo classInfo = classDao.selectById(student.getClassId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("student", student);
        data.put("classInfo", classInfo);
        return Result.success(data);
    }

    @Override
    public Result updatePhone(PhoneUpdateDTO phoneDTO) {
        int rows = studentDao.updatePhone(phoneDTO.getStudentId(), phoneDTO.getPhone());
        return rows > 0 ? Result.success("手机号修改成功") : Result.error("修改失败");
    }

    @Override
    public Result getMyApplication(int studentId) {
        return Result.success(applicationDao.selectByStudentId(studentId));
    }
    
    @Override
    public Result getClassTeachers(int studentId) {
        // 1. 查询学生信息
        StudentAccount student = studentDao.selectById(studentId);
        if (student == null) {
            return Result.error("学生不存在");
        }
        
        // 2. 根据学生的classId查询班级信息
        ClassInfo classInfo = classDao.selectById(student.getClassId());
        if (classInfo == null) {
            return Result.error("班级信息不存在");
        }
        
        // 3. 根据班级的campusId筛选该校区的教师
        List<TeacherAccount> teachers = teacherDao.selectAll();
        List<Map<String, Object>> filteredTeachers = new ArrayList<>();
        
        for (TeacherAccount teacher : teachers) {
            // 只返回同校区的教师（campusId匹配）
            if (teacher.getCampusId() != null && teacher.getCampusId().equals(classInfo.getCampusId())) {
                Map<String, Object> teacherMap = new HashMap<>();
                teacherMap.put("teacherId", teacher.getTeacherId());
                teacherMap.put("realName", teacher.getRealName());
                teacherMap.put("title", teacher.getTitle());
                teacherMap.put("campusId", teacher.getCampusId());
                filteredTeachers.add(teacherMap);
            }
        }
        
        return Result.success(filteredTeachers);
    }
}
