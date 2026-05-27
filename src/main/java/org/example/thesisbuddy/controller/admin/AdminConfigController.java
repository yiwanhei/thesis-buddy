package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dto.admin.TeamLimitDTO;
import org.example.thesisbuddy.service.admin.AdminConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    @Autowired
    private AdminConfigService adminConfigService;

    // 查看系统配置
    @GetMapping("/get")
    public Result getConfig() {
        return adminConfigService.getConfig();
    }

    // 修改选题开关
    @PutMapping("/topic-switch")
    public Result updateTopicSwitch(@RequestParam int status) {
        return adminConfigService.updateTopicSwitch(status);
    }

    // 修改组队开关
    @PutMapping("/team-switch")
    public Result updateTeamSwitch(@RequestParam int status) {
        return adminConfigService.updateTeamSwitch(status);
    }

    // 修改人数限制
    @PutMapping("/team-limit")
    public Result updateTeamLimit(@RequestBody TeamLimitDTO limitDTO) {
        return adminConfigService.updateTeamLimit(limitDTO);
    }
}
