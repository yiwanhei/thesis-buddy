package org.example.thesisbuddy.controller.student;

import org.example.thesisbuddy.common.Result;
import org.example.thesisbuddy.dto.student.TeamCreateDTO;
import org.example.thesisbuddy.dto.student.TeamInviteDTO;
import org.example.thesisbuddy.service.student.StudentTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/team")
public class StudentTeamController {

    @Autowired
    private StudentTeamService studentTeamService;

    // 创建组队
    @PostMapping("/create")
    public Result createTeam(@RequestBody TeamCreateDTO createDTO) {
        return studentTeamService.createTeam(createDTO);
    }

    // 查看组队详情
    @GetMapping("/detail/{teamId}")
    public Result getTeamDetail(@PathVariable int teamId) {
        return studentTeamService.getTeamDetail(teamId);
    }

    // 获取我的队伍信息
    @GetMapping("/my-team")
    public Result getMyTeam(@RequestParam int studentId) {
        return studentTeamService.getMyTeam(studentId);
    }

    // 检查是否为队长
    @GetMapping("/check-captain")
    public Result checkCaptain(@RequestParam int studentId) {
        return studentTeamService.checkCaptain(studentId);
    }

    // 发送组队邀请
    @PostMapping("/invite")
    public Result sendInvite(@RequestBody TeamInviteDTO inviteDTO) {
        return studentTeamService.sendInvite(inviteDTO);
    }

    // 接受/拒绝邀请
    @PutMapping("/invite/handle")
    public Result handleInvite(
            @RequestParam int inviteId,
            @RequestParam String action) {
        return studentTeamService.handleInvite(inviteId, action);
    }

    // 查看我的邀请列表（被邀请）
    @GetMapping("/invites")
    public Result getMyInvites(@RequestParam int studentId) {
        return studentTeamService.getMyInvites(studentId);
    }
    
    // 查看队长发出的邀请列表
    @GetMapping("/sent-invites")
    public Result getSentInvites(@RequestParam int teamId) {
        return studentTeamService.getSentInvites(teamId);
    }

    // 退出组队
    @DeleteMapping("/quit")
    public Result quitTeam(@RequestParam int teamId, @RequestParam int studentId) {
        return studentTeamService.quitTeam(teamId, studentId);
    }
    
    // 踢出成员
    @DeleteMapping("/member/kick")
    public Result kickMember(@RequestParam int teamId, @RequestParam int studentId, @RequestParam int captainId) {
        return studentTeamService.kickMember(teamId, studentId, captainId);
    }
    
    // 更新队伍状态
    @PutMapping("/update-status")
    public Result updateTeamStatus(@RequestParam int teamId, @RequestParam String status) {
        return studentTeamService.updateTeamStatus(teamId, status);
    }
    
    // 解散队伍
    @DeleteMapping("/dissolve")
    public Result dissolveTeam(@RequestParam int teamId, @RequestParam int captainId) {
        return studentTeamService.dissolveTeam(teamId, captainId);
    }
    
    // 更新队伍信息（队名、人数）
    @PutMapping("/update")
    public Result updateTeamInfo(@RequestBody java.util.Map<String, Object> params) {
        int teamId = (int) params.get("teamId");
        String teamName = (String) params.get("teamName");
        int maxMembers = (int) params.get("maxMembers");
        return studentTeamService.updateTeamInfo(teamId, teamName, maxMembers);
    }
    
    // 搜索队伍
    @GetMapping("/search")
    public Result searchTeams(@RequestParam String keyword, @RequestParam int studentId) {
        return studentTeamService.searchTeams(keyword, studentId);
    }
    
    // 提交加入请求
    @PostMapping("/join-request")
    public Result submitJoinRequest(@RequestBody java.util.Map<String, Object> params) {
        int studentId = (int) params.get("studentId");
        int teamId = (int) params.get("teamId");
        String reason = (String) params.getOrDefault("reason", "");
        return studentTeamService.submitJoinRequest(studentId, teamId, reason);
    }
    
    // 获取队伍的加入请求列表（队长查看收到的申请）
    @GetMapping("/join-requests")
    public Result getJoinRequests(@RequestParam int teamId) {
        return studentTeamService.getJoinRequests(teamId);
    }
    
    // 获取自己提交的加入申请列表（单人学生查看自己的申请）
    @GetMapping("/my-join-requests")
    public Result getMyJoinRequests(@RequestParam int studentId) {
        return studentTeamService.getMyJoinRequests(studentId);
    }
    
    // 处理加入请求
    @PutMapping("/join-request/handle")
    public Result handleJoinRequest(@RequestParam int requestId, 
                                    @RequestParam String action,
                                    @RequestParam int teamId,
                                    @RequestParam int captainId) {
        return studentTeamService.handleJoinRequest(requestId, action, teamId, captainId);
    }
    
    // 搜索同班学生
    @GetMapping("/search-students")
    public Result searchStudents(@RequestParam String keyword, @RequestParam int studentId) {
        return studentTeamService.searchStudents(keyword, studentId);
    }
    
    // 更新成员职责
    @PutMapping("/member/task")
    public Result updateMemberTask(@RequestBody java.util.Map<String, Object> params) {
        int teamId = (int) params.get("teamId");
        int studentId = (int) params.get("studentId");
        String task = (String) params.getOrDefault("task", "");
        return studentTeamService.updateMemberTask(teamId, studentId, task);
    }
}
