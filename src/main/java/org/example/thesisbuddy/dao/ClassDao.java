package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.entity.ClassInfo;

import java.util.List;

@Mapper//班级数据操作接口
public interface ClassDao {

    // 根据班级 ID 查询
    ClassInfo selectById(@Param("classId") Integer classId);
    
    // 查询所有班级
    List<ClassInfo> selectAll();
}
