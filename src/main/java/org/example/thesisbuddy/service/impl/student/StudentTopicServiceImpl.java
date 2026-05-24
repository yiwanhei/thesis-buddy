package org.example.thesisbuddy.service.impl.student;

import org.example.thesisbuddy.dao.TopicDao;
import org.example.thesisbuddy.dao.TeacherDao;
import org.example.thesisbuddy.entity.TopicLibrary;
import org.example.thesisbuddy.entity.TeacherAccount;
import org.example.thesisbuddy.service.student.StudentTopicService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class StudentTopicServiceImpl implements StudentTopicService {

    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private TeacherDao teacherDao;

    @Override
    public Result getTopicList(int page, int size, String category) {
        int offset = (page - 1) * size;
        List<TopicLibrary> list = (category != null && !category.isEmpty()) 
                ? topicDao.selectByCategory(category, offset, size) 
                : topicDao.selectAll(offset, size);
        int total = (category != null && !category.isEmpty()) 
                ? topicDao.countByCategory(category) 
                : topicDao.countAll();
        
        // 为每个选题添加剩余名额信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (TopicLibrary topic : list) {
            Map<String, Object> topicMap = new HashMap<>();
            topicMap.put("topicId", topic.getTopicId());
            topicMap.put("topicName", topic.getTopicName());
            topicMap.put("category", topic.getCategory());
            topicMap.put("resultForm", topic.getResultForm());
            topicMap.put("maxCapacity", topic.getMaxCapacity());
            topicMap.put("status", topic.getStatus());
            topicMap.put("createTime", topic.getCreateTime());
            // 动态计算剩余名额
            topicMap.put("remaining", topicDao.selectRemainingCount(topic.getTopicId()));
            resultList.add(topicMap);
        }
                
        Map<String, Object> data = new HashMap<>();
        data.put("list", resultList);
        data.put("total", total);
        data.put("page", page);
        return Result.success(data);
    }

    @Override
    public Result getTopicDetail(int topicId) {
        TopicLibrary topic = topicDao.selectById(topicId);
        if (topic == null) return Result.error("选题不存在");
        
        Map<String, Object> data = new HashMap<>();
        data.put("topic", topic);
        data.put("remaining", topicDao.selectRemainingCount(topicId));
        return Result.success(data);
    }

    @Override
    public Result getRemainingCount(int topicId) {
        int count = topicDao.selectRemainingCount(topicId);
        return count >= 0 ? Result.success(count) : Result.error("选题不存在");
    }
    
    @Override
    public Result getAvailableTopics() {
        try {
            // 查询所有有剩余名额的选题
            List<TopicLibrary> topics = topicDao.selectAvailableTopics();
            
            // 构建返回数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (TopicLibrary topic : topics) {
                Map<String, Object> topicInfo = new HashMap<>();
                topicInfo.put("topicId", topic.getTopicId());
                topicInfo.put("topicName", topic.getTopicName());
                topicInfo.put("category", topic.getCategory());
                topicInfo.put("resultForm", topic.getResultForm());
                topicInfo.put("availableSlots", topicDao.selectRemainingCount(topic.getTopicId()));
                topicInfo.put("teacherName", "待分配");
                
                result.add(topicInfo);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取可选题目失败：" + e.getMessage());
        }
    }
}
