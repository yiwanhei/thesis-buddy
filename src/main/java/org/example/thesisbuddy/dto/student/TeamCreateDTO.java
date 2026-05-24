package org.example.thesisbuddy.dto.student;

import lombok.Data;
import java.util.List;

@Data
public class TeamCreateDTO {
    private int captainId;           // 队长 ID
    private String teamName;         // 队名
    private Integer topicId;         // 申请的题目ID
    private Integer expectedMembers; // 预计人数（默认 2）
    private List<Integer> memberIds; // 初始队员 ID 列表（可选）
}
