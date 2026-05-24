package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    // 查看系统配置
    @GetMapping("/get")
    public Result getConfig() {
        // TODO: 实现查询逻辑
        return Result.success();
    }

    // 修改选题开关
    @PutMapping("/topic-switch")
    public Result updateTopicSwitch(@RequestParam int status) {
        // TODO: 0 关闭 1 开启
        return Result.success();
    }

    // 修改组队开关
    @PutMapping("/team-switch")
    public Result updateTeamSwitch(@RequestParam int status) {
        // TODO: 0 关闭 1 开启
        return Result.success();
    }

    // 修改人数限制
    @PutMapping("/team-limit")
    public Result updateTeamLimit(@RequestBody Object limitDTO) {
        // TODO: 实现修改逻辑
        return Result.success();
    }
}
