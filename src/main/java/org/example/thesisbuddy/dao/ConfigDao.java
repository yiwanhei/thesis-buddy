package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.entity.SystemConfig;

import java.util.List;

@Mapper
public interface ConfigDao {
    
    // 查询所有系统配置
    List<SystemConfig> selectAll();
    
    // 更新配置值
    int updateValue(@Param("key") String key, @Param("value") String value);
}
