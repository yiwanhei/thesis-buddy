package org.example.thesisbuddy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamMember {
    private Integer memberId;
    private Integer teamId;           // 所属组队
    private Integer studentId;        // 学生 ID
    private String roleInTeam;        // 组内职责分工
    private String task;              // 成员职责
    private LocalDateTime joinTime;
}
