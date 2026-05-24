package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.entity.StudentAccount;

import java.util.List;

@Mapper//学生数据操作接口
public interface StudentDao {

    // 根据学号查询学生
    StudentAccount selectByAccount(@Param("account") String account);

    // 根据学生 ID 查询
    StudentAccount selectById(@Param("studentId") Integer studentId);

    // 更新密码
    int updatePassword(@Param("studentId") Integer studentId,
                       @Param("newPassword") String newPassword);

    // 更新手机号
    int updatePhone(@Param("studentId") Integer studentId,
                    @Param("phone") String phone);
    
    // 插入学生
    int insert(StudentAccount student);
    
    // 更新学生状态
    int updateStatus(@Param("studentId") Integer studentId, @Param("status") Integer status);
    
    // 查询所有学生
    List<StudentAccount> selectAll();
    
    // 统计学生总数
    int countAll();
}
