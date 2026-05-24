package org.example.thesisbuddy.service.student;

import org.example.thesisbuddy.dto.student.TeamCreateDTO;
import org.example.thesisbuddy.dto.student.TeamInviteDTO;
import org.example.thesisbuddy.common.Result;

public interface StudentTeamService {
    // 创建组队
    Result createTeam(TeamCreateDTO createDTO);

    // 查看组队详情
    Result getTeamDetail(int teamId);

    // 获取我的队伍信息
    Result getMyTeam(int studentId);

    // 检查是否为队长
    Result checkCaptain(int studentId);

    // 发送组队邀请
    Result sendInvite(TeamInviteDTO inviteDTO);

    // 处理邀请（接受/拒绝）
    Result handleInvite(int inviteId, String action);

    // 查看我的邀请列表
    Result getMyInvites(int studentId);

    // 退出组队
    Result quitTeam(int teamId, int studentId);
    
    // 踢出成员
    Result kickMember(int teamId, int studentId, int captainId);
    
    // 更新队伍状态
    Result updateTeamStatus(int teamId, String status);
    
    // 加入队伍申请
    Result joinTeam(int studentId, int teamId, String reason);
    
    // 解散队伍
    Result dissolveTeam(int teamId, int captainId);
    
    // 更新队伍信息
    Result updateTeamInfo(int teamId, String teamName, int maxMembers);
    
    // 搜索队伍
    Result searchTeams(String keyword, int studentId);
    
    // 提交加入请求
    Result submitJoinRequest(int studentId, int teamId, String reason);
    
    // 获取队伍的加入请求列表
    Result getJoinRequests(int teamId);
    
    // 处理加入请求
    Result handleJoinRequest(int requestId, String action, int teamId, int captainId);
    
    // 搜索同班学生
    Result searchStudents(String keyword, int studentId);
    
    // 更新成员职责
    Result updateMemberTask(int teamId, int studentId, String task);
}
