package org.example.thesisbuddy.controller.admin;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dao.TeamDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/team")
public class AdminTeamController {

    @Autowired
    private TeamDao teamDao;

    @Autowired
    private ApplicationDao applicationDao;

    // 分页查询队伍列表
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        Map<String, Object> data = new HashMap<>();
        data.put("list", teamDao.selectAllTeams(offset, size));
        data.put("total", teamDao.countAllTeams());
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    // 解散队伍
    @PostMapping("/dissolve/{teamId}")
    public Result dissolve(@PathVariable int teamId) {
        // 1. 检查队伍是否存在
        var team = teamDao.selectById(teamId);
        if (team == null) {
            return Result.error("队伍不存在");
        }

        // 2. 检查是否有已确认的申请，不允许解散
        var confirmedApps = applicationDao.selectByTeamIdAndStatus(teamId, 3);
        if (confirmedApps != null && !confirmedApps.isEmpty()) {
            return Result.error("该队伍已有已确认的选题申请，无法解散");
        }

        // 3. 释放所有成员的预占记录
        applicationDao.releaseTeamAllReservations(teamId, "管理员解散队伍");
        // 4. 删除所有成员
        teamDao.deleteAllMembers(teamId);
        // 5. 删除队伍
        teamDao.deleteTeam(teamId);
        return Result.success("队伍已解散");
    }

    // 查看队伍详情
    @GetMapping("/detail/{teamId}")
    public Result detail(@PathVariable int teamId) {
        Map<String, Object> data = new HashMap<>();
        data.put("team", teamDao.selectById(teamId));
        data.put("members", teamDao.selectMembersByTeamId(teamId));
        return Result.success(data);
    }
}
