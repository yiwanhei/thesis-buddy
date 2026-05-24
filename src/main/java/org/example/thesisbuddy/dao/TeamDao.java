package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.dto.student.TeamInviteDTO;
import org.example.thesisbuddy.entity.TeamInfo;
import org.example.thesisbuddy.entity.TeamMember;

import java.util.List;

@Mapper
public interface TeamDao {
    
    // 插入组队信息
    @Options(useGeneratedKeys = true, keyProperty = "teamId")
    int insertTeam(TeamInfo team);
    
    // 根据ID查询组队
    TeamInfo selectById(int teamId);
    
    // 插入队员
    int insertMember(TeamMember member);
    
    // 查询组队的所有成员
    List<TeamMember> selectMembersByTeamId(int teamId);
    
    // 检查学生是否已是队员
    boolean isMember(@Param("teamId") int teamId, @Param("studentId") int studentId);
    
    // 插入组队邀请
    int insertInvite(TeamInviteDTO inviteDTO);
    
    // 更新邀请状态
    int updateInviteStatus(@Param("inviteId") int inviteId, @Param("action") String action);
    
    // 查询学生收到的邀请
    List<TeamInviteDTO> selectInvitesByStudent(int studentId);
    
    // 根据邀请ID查询邀请信息
    TeamInviteDTO selectInviteById(int inviteId);
    
    // 删除队员
    int deleteMember(@Param("teamId") int teamId, @Param("studentId") int studentId);
    
    // 根据学生ID查询所在队伍（作为队长）
    TeamInfo selectTeamByCaptainId(int captainId);
    
    // 根据学生ID查询所在队伍（作为队员）
    TeamInfo selectTeamByMemberId(int studentId);
    
    // 查询队伍的成员数量
    int countMembersByTeamId(int teamId);
    
    // 查询所有已过期的队伍（reserve_until < 当前时间）
    List<TeamInfo> selectExpiredTeams();
    
    // 更新队伍状态为已释放
    int updateTeamStatus(@Param("teamId") int teamId, @Param("status") String status);
    
    // 删除队伍的所有成员
    int deleteAllMembers(int teamId);
    
    // 删除队伍
    int deleteTeam(int teamId);
    
    // 更新队伍信息（队名和最大人数）
    int updateTeamInfo(@Param("teamId") int teamId, @Param("teamName") String teamName, @Param("maxMembers") int maxMembers);
    
    // 搜索队伍（通过队长学号或选题名称，限制同班级）
    List<TeamInfo> searchTeams(@Param("keyword") String keyword, @Param("classId") Integer classId);
    
    // 查询队伍的加入请求
    List<java.util.Map<String, Object>> selectJoinRequests(@Param("teamId") int teamId);
    
    // 插入加入请求
    int insertJoinRequest(@Param("studentId") int studentId, @Param("teamId") int teamId, @Param("reason") String reason);
    
    // 更新加入请求状态
    int updateJoinRequestStatus(@Param("requestId") int requestId, @Param("status") String status);
    
    // 查询学生信息（用于搜索）
    List<java.util.Map<String, Object>> searchStudents(@Param("keyword") String keyword, @Param("classId") Integer classId);
    
    // 更新成员职责
    int updateMemberTask(@Param("teamId") int teamId, @Param("studentId") int studentId, @Param("task") String task);
    
    // 根据学生ID查询学生姓名
    String selectStudentName(@Param("studentId") int studentId);
    
    // 查询学生的班级ID
    Integer selectStudentClassId(@Param("studentId") int studentId);
}
