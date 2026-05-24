package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.thesisbuddy.entity.AdminAccount;

@Mapper
public interface AdminDao {
    
    // 根据账号查询管理员
    AdminAccount selectByAccount(String account);
}
