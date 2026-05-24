package org.example.thesisbuddy.service.admin;

import org.example.thesisbuddy.dto.admin.TeamLimitDTO;
import org.example.thesisbuddy.common.Result;

public interface AdminConfigService {
    // 查看系统配置
    Result getConfig();

    // 修改选题开关
    Result updateTopicSwitch(int status);

    // 修改组队开关
    Result updateTeamSwitch(int status);

    // 修改人数限制
    Result updateTeamLimit(TeamLimitDTO limitDTO);
}
