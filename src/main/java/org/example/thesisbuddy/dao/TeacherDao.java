package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.entity.TeacherAccount;

import java.util.List;

@Mapper
public interface TeacherDao {
    
    // 根据工号查询教师
    TeacherAccount selectByAccount(@Param("account") String account);
    
    // 根据教师ID查询
    TeacherAccount selectById(@Param("teacherId") Integer teacherId);
    
    // 插入教师
    int insert(TeacherAccount teacher);
    
    // 更新教师状态
    int updateStatus(@Param("teacherId") Integer teacherId, @Param("status") Integer status);
    
    // 查询所有教师
    List<TeacherAccount> selectAll();
    
    // 统计教师总数
    int countAll();
}
