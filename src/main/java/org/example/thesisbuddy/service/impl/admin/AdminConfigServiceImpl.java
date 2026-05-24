package org.example.thesisbuddy.service.impl.admin;

import org.example.thesisbuddy.dao.ConfigDao;
import org.example.thesisbuddy.dto.admin.TeamLimitDTO;
import org.example.thesisbuddy.service.admin.AdminConfigService;
import org.example.thesisbuddy.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminConfigServiceImpl implements AdminConfigService {

    @Autowired
    private ConfigDao configDao;

    @Override
    public Result getConfig() {
        return Result.success(configDao.selectAll());
    }

    @Override
    public Result updateTopicSwitch(int status) {
        return configDao.updateValue("topic_selection_enabled", String.valueOf(status)) > 0 
                ? Result.success() : Result.error("更新失败");
    }

    @Override
    public Result updateTeamSwitch(int status) {
        return configDao.updateValue("team_invite_enabled", String.valueOf(status)) > 0 
                ? Result.success() : Result.error("更新失败");
    }

    @Override
    public Result updateTeamLimit(TeamLimitDTO limitDTO) {
        configDao.updateValue("max_team_members", String.valueOf(limitDTO.getMaxTeamMembers()));
        configDao.updateValue("min_team_members", String.valueOf(limitDTO.getMinTeamMembers()));
        return Result.success();
    }
}
