package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.entity.TopicLibrary;

import java.util.List;
import java.util.Map;

@Mapper//选题数据操作接口
public interface TopicDao {

    // 查询所有选题（分页）
    List<TopicLibrary> selectAll(@Param("offset") int offset,
                                 @Param("limit") int limit);

    // 按类别筛选选题
    List<TopicLibrary> selectByCategory(@Param("category") String category,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    // 查询选题总数
    int countAll();

    // 按类别统计总数
    int countByCategory(@Param("category") String category);

    // 根据选题 ID 查询
    TopicLibrary selectById(@Param("topicId") Integer topicId);

    // 查询选题剩余人数（动态计算）
    int selectRemainingCount(@Param("topicId") Integer topicId);
    
    // 插入选题
    int insert(TopicLibrary topic);
    
    // 更新选题状态
    int updateStatus(@Param("topicId") Integer topicId, @Param("status") String status);
    
    // 查询教师的选题统计
    Map<String, Object> selectStatsByTeacher(int teacherId);
    
    // 查询所有有剩余名额的选题（用于创建队伍时选择）
    List<TopicLibrary> selectAvailableTopics();
    
    // 更新选题
    int update(TopicLibrary topic);
    
    // 删除选题
    int deleteById(@Param("topicId") Integer topicId);
}
