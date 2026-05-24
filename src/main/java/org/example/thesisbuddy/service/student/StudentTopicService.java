package org.example.thesisbuddy.service.student;

import org.example.thesisbuddy.common.Result;

public interface StudentTopicService {
    // 查询选题库（分页 + 筛选）
    Result getTopicList(int page, int size, String category);

    // 查看选题详情
    Result getTopicDetail(int topicId);

    // 查看选题剩余人数
    Result getRemainingCount(int topicId);
    
    // 获取可选题目列表（用于创建队伍时选择）
    Result getAvailableTopics();
}
