package org.example.thesisbuddy.dto.admin;

import lombok.Data;

@Data//修改人数限制
public class TeamLimitDTO {
    private Integer maxTeamMembers;  // 最大组队人数
    private Integer minTeamMembers;  // 最小组队人数
}
