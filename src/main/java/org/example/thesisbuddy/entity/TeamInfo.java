package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamInfo {
    private Integer teamId;
    private String teamName;          // 队名
    private Integer captainId;        // 队长 ID
    private Integer maxMembers;       // 最大人数
    private String applyTopic;        // 申请题目
    private Integer topicId;          // 选题ID
    private Integer expectedMembers;  // 预计人数
    private LocalDateTime reserveUntil; // 名额保留截止时间（已废弃，改用create_time+30分钟）
    private String status;            // forming(组建中)/confirmed(已确认)
    private LocalDateTime createTime;
}
