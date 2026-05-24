package org.example.thesisbuddy.service.student;

import org.example.thesisbuddy.dto.student.PhoneUpdateDTO;
import org.example.thesisbuddy.common.Result;

public interface StudentProfileService {
    // 查看个人信息
    Result getMyProfile(int studentId);

    // 修改手机号
    Result updatePhone(PhoneUpdateDTO phoneDTO);

    // 查看我的选题记录
    Result getMyApplication(int studentId);
    
    // 获取本班级的教师列表（用于指导教师选择）
    Result getClassTeachers(int studentId);
}
