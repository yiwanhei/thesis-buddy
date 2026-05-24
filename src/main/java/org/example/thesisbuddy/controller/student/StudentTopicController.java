package org.example.thesisbuddy.controller.student;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.service.student.StudentTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/topic")
public class StudentTopicController {

    @Autowired
    private StudentTopicService studentTopicService;

    // 查询选题库（分页 + 按类别筛选）
    @GetMapping("/list")
    public Result getTopicList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        return studentTopicService.getTopicList(page, size, category);
    }

    // 查看选题详情
    @GetMapping("/detail/{topicId}")
    public Result getTopicDetail(@PathVariable int topicId) {
        return studentTopicService.getTopicDetail(topicId);
    }

    // 查看选题剩余人数
    @GetMapping("/remaining/{topicId}")
    public Result getRemainingCount(@PathVariable int topicId) {
        return studentTopicService.getRemainingCount(topicId);
    }
    
    // 获取可选题目列表（用于创建队伍时选择）
    @GetMapping("/available")
    public Result getAvailableTopics() {
        return studentTopicService.getAvailableTopics();
    }
}
