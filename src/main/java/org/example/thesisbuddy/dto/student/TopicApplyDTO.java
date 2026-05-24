package org.example.thesisbuddy.dto.student;

import lombok.Data;

@Data//提交选题申请
public class TopicApplyDTO {
    private int studentId;     // 学生 ID
    private int topicId;       // 选题 ID
    private String applyType;  // 申请类型：individual（个人）/team（组队）
    private Integer teamId;    // 组队 ID（组队选题时必填，个人选题为 null）
}
