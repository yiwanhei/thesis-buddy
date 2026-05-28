package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data//选题库实体
public class TopicLibrary {
    private Integer topicId;
    private String topicName;      // 选题名称
    private String category;       // 类别
    private String resultForm;     // 成果形式
    private Integer maxCapacity;   // 最大人数
    private String status;         // open/closed
    private LocalDateTime createTime;
    private Integer teacherId;     // 指导教师ID
}
