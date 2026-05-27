package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.TopicDao;
import org.example.thesisbuddy.entity.TopicLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/topic")
public class AdminTopicController {

    @Autowired
    private TopicDao topicDao;

    @Autowired
    private ApplicationDao applicationDao;

    // 分页查询选题列表
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String category) {
        int offset = (page - 1) * size;
        Map<String, Object> data = new HashMap<>();
        if (category != null && !category.isEmpty()) {
            data.put("list", topicDao.selectByCategory(category, offset, size));
            data.put("total", topicDao.countByCategory(category));
        } else {
            data.put("list", topicDao.selectAll(offset, size));
            data.put("total", topicDao.countAll());
        }
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    // 添加选题
    @PostMapping("/add")
    public Result add(@RequestBody TopicLibrary topic) {
        topic.setCreateTime(LocalDateTime.now());
        if (topic.getStatus() == null) topic.setStatus("open");
        return topicDao.insert(topic) > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 更新选题
    @PutMapping("/update")
    public Result update(@RequestBody TopicLibrary topic) {
        return topicDao.update(topic) > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 切换选题开关状态
    @PutMapping("/status")
    public Result toggleStatus(@RequestParam int topicId, @RequestParam String status) {
        return topicDao.updateStatus(topicId, status) > 0 ? Result.success("操作成功") : Result.error("操作失败");
    }

    // 删除选题
    @DeleteMapping("/{topicId}")
    public Result delete(@PathVariable Integer topicId) {
        // 检查是否有申请记录引用该选题
        int occupied = applicationDao.countOccupiedByTopic(topicId);
        if (occupied > 0) {
            return Result.error("该选题下已有申请记录，无法删除");
        }
        return topicDao.deleteById(topicId) > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
}
