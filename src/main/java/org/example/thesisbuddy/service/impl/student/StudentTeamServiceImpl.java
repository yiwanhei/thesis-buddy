 package org.example.thesisbuddy.service.impl.student;

import org.example.thesisbuddy.dao.*;
import org.example.thesisbuddy.dto.student.TeamCreateDTO;
import org.example.thesisbuddy.dto.student.TeamInviteDTO;
import org.example.thesisbuddy.entity.*;
import org.example.thesisbuddy.service.student.StudentTeamService;
import org.example.thesisbuddy.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StudentTeamServiceImpl implements StudentTeamService {

    private static final Logger logger = LoggerFactory.getLogger(StudentTeamServiceImpl.class);

    @Autowired
    private TeamDao teamDao;
    
    @Autowired
    private StudentDao studentDao;
    
    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private ApplicationDao applicationDao;
    
    @Autowired
    private ConfigDao configDao;
    
    private boolean isTopicSelectionEnabled() {
        String value = configDao.selectByKey("topic_selection_enabled");
        return "1".equals(value);
    }
    
    private LocalDateTime calcReserveUntil() {
        return isTopicSelectionEnabled() ? LocalDateTime.now().plusMinutes(30) : null;
    }

    @Override
    @Transactional
    public Result createTeam(TeamCreateDTO createDTO) {
        if (studentDao.selectById(createDTO.getCaptainId()) == null) {
            return Result.error("队长不存在");
        }
        
        // 验证预计人数
        Integer expectedMembers = createDTO.getExpectedMembers();
        if (expectedMembers == null || expectedMembers < 2 || expectedMembers > 5) {
            return Result.error("预计人数必须在2-5人之间");
        }
        
        boolean topicEnabled = isTopicSelectionEnabled();
        String applyTopic = null;
        Integer topicId = null;
        
        if (topicEnabled) {
            // 选题开启：topicId 必填，检查剩余名额
            if (createDTO.getTopicId() == null) {
                return Result.error("选题功能已开启，请选择题目");
            }
            TopicLibrary topic = topicDao.selectById(createDTO.getTopicId());
            if (topic == null) {
                return Result.error("选题不存在");
            }
            int occupied = applicationDao.countOccupiedByTopic(createDTO.getTopicId());
            int remaining = topic.getMaxCapacity() - occupied;
            if (remaining < expectedMembers) {
                return Result.error("该选题剩余名额不足，需要" + expectedMembers + "个，仅剩" + remaining + "个");
            }
            applyTopic = topic.getTopicName();
            topicId = topic.getTopicId();
        }
        
        TeamInfo team = new TeamInfo();
        team.setCaptainId(createDTO.getCaptainId());
        team.setTeamName("队伍" + createDTO.getCaptainId());
        team.setApplyTopic(applyTopic);
        team.setTopicId(topicId);
        team.setExpectedMembers(expectedMembers);
        team.setMaxMembers(expectedMembers);
        team.setStatus("forming");
        
        int rows = teamDao.insertTeam(team);
        if (rows <= 0) return Result.error("创建失败");
        Integer teamId = team.getTeamId();
        
        // 添加队长为成员
        TeamMember captain = new TeamMember();
        captain.setTeamId(teamId);
        captain.setStudentId(createDTO.getCaptainId());
        captain.setRoleInTeam("队长");
        teamDao.insertMember(captain);
        
        // 收集所有实际成员ID（队长 + 初始队员）
        List<Integer> actualMembers = new ArrayList<>();
        actualMembers.add(createDTO.getCaptainId());
        if (createDTO.getMemberIds() != null) {
            for (Integer sid : createDTO.getMemberIds()) {
                if (!actualMembers.contains(sid)) {
                    TeamMember m = new TeamMember();
                    m.setTeamId(teamId);
                    m.setStudentId(sid);
                    m.setRoleInTeam("组员");
                    teamDao.insertMember(m);
                    actualMembers.add(sid);
                }
            }
        }
        
        if (topicEnabled && topicId != null) {
            // 选题开启：为 expectedMembers 个名额创建预占记录
            LocalDateTime deadline = LocalDateTime.now().plusMinutes(30);
            // 先为实际成员创建预占（检查并处理已有申请记录）
            for (Integer sid : actualMembers) {
                // 检查该学生是否已有任何申请记录（不限状态）
                ThesisApplication existing = applicationDao.selectByStudentId(sid);
                if (existing != null) {
                    Integer oldStatus = existing.getApplicationStatus();
                    if (oldStatus != null && oldStatus == 3) {
                        // 已通过状态不能重复申请
                        return Result.error("该学生已有已确认的选题申请");
                    }
                    // 其他状态（0=已释放/过期，1=预占中，2=审核中）：UPDATE 复用已有记录
                    applicationDao.updateByStudentId(sid, topicId, teamId, "team", 1, 0,
                        LocalDateTime.now(), deadline);
                    continue;
                }
                ThesisApplication app = new ThesisApplication();
                app.setStudentId(sid);
                app.setTopicId(topicId);
                app.setTeamId(teamId);
                app.setApplyType("team");
                app.setApplicationStatus(1);
                app.setIsLocked(0);
                app.setApplyTime(LocalDateTime.now());
                app.setReserveUntil(deadline);
                applicationDao.insert(app);
            }
            // 多余名额创建设占位记录（student_id = -1 表示未分配名额）
            int placeholderCount = expectedMembers - actualMembers.size();
            for (int i = 0; i < placeholderCount; i++) {
                ThesisApplication app = new ThesisApplication();
                app.setStudentId(-1);
                app.setTopicId(topicId);
                app.setTeamId(teamId);
                app.setApplyType("team");
                app.setApplicationStatus(1);
                app.setIsLocked(0);
                app.setApplyTime(LocalDateTime.now());
                app.setReserveUntil(deadline);
                applicationDao.insert(app);
            }
            return Result.success("队伍创建成功，名额已保留30分钟");
        }
        
        return Result.success("队伍创建成功");
    }

    @Override
    public Result getTeamDetail(int teamId) {
        TeamInfo team = teamDao.selectById(teamId);
        if (team == null) return Result.error("组队不存在");
        
        List<Map<String, Object>> members = teamDao.selectMembersByTeamId(teamId);
        
        // 确保成员信息中包含realName字段（驼峰命名）
        if (members != null) {
            for (Map<String, Object> member : members) {
                if (member.containsKey("real_name") && !member.containsKey("realName")) {
                    member.put("realName", member.get("real_name"));
                }
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("team", team);
        data.put("members", members);
        return Result.success(data);
    }

    @Override
    public Result getMyTeam(int studentId) {
        // 先查找作为队长的队伍
        TeamInfo team = teamDao.selectTeamByCaptainId(studentId);
        
        // 如果不是队长，查找作为队员的队伍
        if (team == null) {
            team = teamDao.selectTeamByMemberId(studentId);
        }
        
        if (team == null) {
            return Result.success(null);
        }
        
        List<Map<String, Object>> members = teamDao.selectMembersByTeamId(team.getTeamId());
        int memberCount = teamDao.countMembersByTeamId(team.getTeamId());
        
        // 确保成员信息中包含realName字段（驼峰命名）
        if (members != null) {
            for (Map<String, Object> member : members) {
                if (member.containsKey("real_name") && !member.containsKey("realName")) {
                    member.put("realName", member.get("real_name"));
                }
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("team", team);
        data.put("members", members);
        data.put("memberCount", memberCount);
        
        // 查询队伍最近的预占到期时间（取最早的 reserve_until）
        if (team.getTopicId() != null) {
            List<ThesisApplication> teamApps = applicationDao.selectByTeamIdAndStatus(team.getTeamId(), 1);
            LocalDateTime earliestDeadline = null;
            for (ThesisApplication app : teamApps) {
                if (app.getReserveUntil() != null && app.getStudentId() > 0) {
                    if (earliestDeadline == null || app.getReserveUntil().isBefore(earliestDeadline)) {
                        earliestDeadline = app.getReserveUntil();
                    }
                }
            }
            if (earliestDeadline != null) {
                long remainingSeconds = java.time.Duration.between(
                    LocalDateTime.now(), earliestDeadline
                ).getSeconds();
                if (remainingSeconds > 0) {
                    data.put("reserveUntil", remainingSeconds);
                }
            }
        }
        
        return Result.success(data);
    }

    @Override
    public Result checkCaptain(int studentId) {
        TeamInfo team = teamDao.selectTeamByCaptainId(studentId);
        
        Map<String, Object> data = new HashMap<>();
        if (team != null) {
            data.put("isCaptain", true);
            int memberCount = teamDao.countMembersByTeamId(team.getTeamId());
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("teamId", team.getTeamId());
            teamInfo.put("teamName", team.getTeamName());
            teamInfo.put("status", team.getStatus());
            teamInfo.put("memberCount", memberCount);
            teamInfo.put("maxMembers", team.getMaxMembers());
            data.put("teamInfo", teamInfo);
        } else {
            data.put("isCaptain", false);
            data.put("teamInfo", null);
        }
        
        return Result.success(data);
    }

    @Override
    public Result sendInvite(TeamInviteDTO inviteDTO) {
        if (studentDao.selectById(inviteDTO.getInviteeId()) == null) {
            return Result.error("该学生不存在");
        }
        
        if (teamDao.isMember(inviteDTO.getTeamId(), inviteDTO.getInviteeId())) {
            return Result.error("该学生已是队员");
        }
        
        int rows = teamDao.insertInvite(inviteDTO);
        return rows > 0 ? Result.success("邀请已发送") : Result.error("发送失败");
    }

    @Override
    @Transactional
    public Result handleInvite(int inviteId, String action) {
        TeamInviteDTO targetInvite = teamDao.selectInviteById(inviteId);
        if (targetInvite == null) {
            return Result.error("邀请不存在");
        }
        
        int rows = teamDao.updateInviteStatus(inviteId, action);
        if (rows <= 0) return Result.error("操作失败");
        
        if ("accepted".equals(action)) {
            if (teamDao.isMember(targetInvite.getTeamId(), targetInvite.getInviteeId())) {
                return Result.error("该学生已是队员");
            }
            
            TeamInfo team = teamDao.selectById(targetInvite.getTeamId());
            int memberCount = teamDao.countMembersByTeamId(targetInvite.getTeamId());
            if (memberCount >= team.getMaxMembers()) {
                return Result.error("队伍人数已满");
            }
            
            // 检查该学生是否已有预占记录（如果有，先清除单人选题记录）
            ThesisApplication existingReservation = applicationDao.selectByStudentIdAndStatus(targetInvite.getInviteeId(), 1);
            if (existingReservation != null) {
                // 如果有单人预占记录，释放原来的记录再接受邀请
                applicationDao.releaseStudentReservation(targetInvite.getInviteeId(), "接受邀请，清除原单人选题预占");
            }
            
            // 添加为队员
            TeamMember member = new TeamMember();
            member.setTeamId(targetInvite.getTeamId());
            member.setStudentId(targetInvite.getInviteeId());
            member.setRoleInTeam("组员");
            teamDao.insertMember(member);
            
            // 队伍有选题时，分配占位记录或新建预占
            if (team.getTopicId() != null) {
                int assigned = applicationDao.assignPlaceholder(
                    targetInvite.getTeamId(),
                    targetInvite.getInviteeId(),
                    calcReserveUntil()
                );
                // 没有占位记录（选题关闭时创建的无预占队伍），新建预占
                if (assigned <= 0) {
                    ThesisApplication newApp = new ThesisApplication();
                    newApp.setStudentId(targetInvite.getInviteeId());
                    newApp.setTopicId(team.getTopicId());
                    newApp.setTeamId(targetInvite.getTeamId());
                    newApp.setApplyType("team");
                    newApp.setApplicationStatus(1);
                    newApp.setIsLocked(0);
                    newApp.setApplyTime(LocalDateTime.now());
                    newApp.setReserveUntil(calcReserveUntil());
                    applicationDao.insert(newApp);
                }
            }
        }
        
        return Result.success("操作成功");
    }

    @Override
    public Result getMyInvites(int studentId) {
        return Result.success(teamDao.selectInvitesByStudent(studentId));
    }
    
    @Override
    public Result getSentInvites(int teamId) {
        return Result.success(teamDao.selectSentInvitesByCaptain(teamId));
    }

    @Override
    public Result quitTeam(int teamId, int studentId) {
        TeamInfo team = teamDao.selectById(teamId);
        if (team == null) {
            return Result.error("队伍不存在");
        }
        
        // 检查队伍是否已确认（选题申请通过）
        if ("confirmed".equals(team.getStatus())) {
            return Result.error("队伍已确认选题，无法退出");
        }
        
        if (team.getCaptainId().equals(studentId)) {
            return Result.error("队长不能退出队伍");
        }
        
        int rows = teamDao.deleteMember(teamId, studentId);
        if (rows <= 0) return Result.error("退出失败");
        
        // 退出/踢出时：将预占记录转为占位（student_id=-1），保留名额和时间不受影响
        applicationDao.convertToPlaceholder(teamId, studentId, "成员退出队伍，名额转为占位");
        
        return Result.success("已退出队伍");
    }

    @Override
    public Result kickMember(int teamId, int studentId, int captainId) {
        TeamInfo team = teamDao.selectById(teamId);
        if (team == null) {
            return Result.error("队伍不存在");
        }
        
        // 检查是否为队长
        if (!team.getCaptainId().equals(captainId)) {
            return Result.error("只有队长可以踢出成员");
        }
        
        // 检查队伍是否已确认
        if ("confirmed".equals(team.getStatus())) {
            return Result.error("队伍已确认选题，无法踢出成员");
        }
        
        // 不能踢出队长自己
        if (team.getCaptainId().equals(studentId)) {
            return Result.error("不能踢出队长");
        }
        
        int rows = teamDao.deleteMember(teamId, studentId);
        if (rows <= 0) return Result.error("踢出失败");
        
        // 踢出时：将预占记录转为占位（student_id=-1），保留名额和时间不受影响
        applicationDao.convertToPlaceholder(teamId, studentId, "成员被踢出队伍，名额转为占位");
        
        return Result.success("已踢出成员");
    }

    @Override
    public Result updateTeamStatus(int teamId, String status) {
        try {
            logger.info("开始更新队伍状态: teamId={}, status={}", teamId, status);
            
            TeamInfo team = teamDao.selectById(teamId);
            if (team == null) {
                logger.error("队伍不存在: teamId={}", teamId);
                return Result.error("队伍不存在");
            }
            
            logger.info("当前队伍信息: teamId={}, teamName={}, currentStatus={}, memberCount={}", 
                teamId, team.getTeamName(), team.getStatus(), team.getMaxMembers());
            
            if (!"forming".equals(team.getStatus())) {
                logger.warn("当前状态不允许转换: teamId={}, currentStatus={}", teamId, team.getStatus());
                return Result.error("当前状态不允许转换");
            }
            
            if (!"confirmed".equals(status)) {
                logger.warn("只能转换为confirmed状态: status={}", status);
                return Result.error("只能转换为confirmed状态");
            }
            
            int rows = teamDao.updateTeamStatus(teamId, status);
            logger.info("更新队伍状态成功: teamId={}, rows={}, newStatus={}", teamId, rows, status);
            
            return rows > 0 ? Result.success("状态更新成功") : Result.error("状态更新失败");
        } catch (Exception e) {
            logger.error("更新队伍状态异常: teamId={}, status={}, error={}", teamId, status, e.getMessage(), e);
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result dissolveTeam(int teamId, int captainId) {
        TeamInfo team = teamDao.selectById(teamId);
        if (team == null) {
            return Result.error("队伍不存在");
        }
        
        // 检查是否为队长
        if (!team.getCaptainId().equals(captainId)) {
            return Result.error("只有队长可以解散队伍");
        }
        
        // 检查队伍是否已确认
        if ("confirmed".equals(team.getStatus())) {
            return Result.error("队伍已确认选题，无法解散");
        }
        
        // 释放所有队员的预占记录
        applicationDao.releaseTeamAllReservations(teamId, "队伍解散，释放所有名额");
        // 删除所有成员
        teamDao.deleteAllMembers(teamId);
        // 删除队伍
        teamDao.deleteTeam(teamId);
        
        return Result.success("队伍已解散");
    }

    @Override
    public Result updateTeamInfo(int teamId, String teamName, int maxMembers) {
        TeamInfo team = teamDao.selectById(teamId);
        if (team == null) {
            return Result.error("队伍不存在");
        }
        
        // 检查队伍状态
        if ("confirmed".equals(team.getStatus())) {
            return Result.error("队伍已确认选题，无法修改");
        }
        
        // 验证队名
        if (teamName == null || teamName.trim().isEmpty()) {
            return Result.error("队名不能为空");
        }
        if (teamName.length() > 20) {
            return Result.error("队名最长20字符");
        }
        
        // 验证人数
        if (maxMembers < 2 || maxMembers > 5) {
            return Result.error("人数必须在2-5人之间");
        }
        
        int rows = teamDao.updateTeamInfo(teamId, teamName.trim(), maxMembers);
        return rows > 0 ? Result.success("队伍信息更新成功") : Result.error("更新失败");
    }

    @Override
    public Result searchTeams(String keyword, int studentId) {
        try {
            // 查询当前学生的班级ID
            Integer classId = teamDao.selectStudentClassId(studentId);
            if (classId == null) {
                return Result.error("未找到班级信息");
            }
            
            List<TeamInfo> teams = teamDao.searchTeams(keyword, classId);
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (TeamInfo team : teams) {
                // 只允许加入forming状态的队伍
                if (!"forming".equals(team.getStatus())) {
                    continue;
                }
                
                Map<String, Object> teamInfo = new HashMap<>();
                teamInfo.put("teamId", team.getTeamId());
                teamInfo.put("applyTopic", team.getApplyTopic());
                teamInfo.put("maxMembers", team.getMaxMembers());
                int memberCount = teamDao.countMembersByTeamId(team.getTeamId());
                teamInfo.put("memberCount", memberCount);
                
                // 获取队长姓名
                String captainName = teamDao.selectStudentName(team.getCaptainId());
                teamInfo.put("captainName", captainName != null ? captainName : team.getCaptainId().toString());
                
                result.add(teamInfo);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            logger.error("搜索队伍失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    @Override
    public Result submitJoinRequest(int studentId, int teamId, String reason) {
        // 检查队伍是否存在
        TeamInfo team = teamDao.selectById(teamId);
        if (team == null) {
            return Result.error("队伍不存在");
        }
        
        // 检查队伍状态
        if (!"forming".equals(team.getStatus())) {
            return Result.error("队伍已确认选题，无法加入");
        }
        
        // 检查是否已是队员
        if (teamDao.isMember(teamId, studentId)) {
            return Result.error("您已是该队伍成员");
        }
        
        // 检查队伍人数是否已满
        int memberCount = teamDao.countMembersByTeamId(teamId);
        if (memberCount >= team.getMaxMembers()) {
            return Result.error("队伍人数已满");
        }
        
        // 插入加入请求
        int rows = teamDao.insertJoinRequest(studentId, teamId, reason);
        return rows > 0 ? Result.success("申请已提交，等待队长审核") : Result.error("申请失败");
    }

    @Override
    public Result getJoinRequests(int teamId) {
        try {
            List<Map<String, Object>> requests = teamDao.selectJoinRequests(teamId);
            return Result.success(requests);
        } catch (Exception e) {
            logger.error("获取加入请求失败", e);
            return Result.error("获取失败");
        }
    }

    @Override
    public Result getMyJoinRequests(int studentId) {
        try {
            List<Map<String, Object>> requests = teamDao.selectMyJoinRequests(studentId);
            return Result.success(requests);
        } catch (Exception e) {
            logger.error("获取我的加入申请记录失败", e);
            return Result.error("获取失败");
        }
    }

    @Override
    public Result handleJoinRequest(int requestId, String action, int teamId, int captainId) {
        try {
            // 验证是否为队长
            TeamInfo team = teamDao.selectById(teamId);
            if (team == null) {
                return Result.error("队伍不存在");
            }
            
            if (!team.getCaptainId().equals(captainId)) {
                return Result.error("只有队长可以处理加入请求");
            }
            
            // 查询请求信息获取studentId
            List<Map<String, Object>> allRequests = teamDao.selectJoinRequests(teamId);
            Integer studentId = null;
            for (Map<String, Object> req : allRequests) {
                if (req.get("id") != null && ((Number)req.get("id")).intValue() == requestId) {
                    studentId = (Integer) req.get("student_id");
                    break;
                }
            }
            
            if (studentId == null) {
                return Result.error("请求不存在");
            }
            
            // 更新请求状态
            int rows = teamDao.updateJoinRequestStatus(requestId, action);
            if (rows <= 0) {
                return Result.error("操作失败");
            }
            
            // 如果同意，将学生添加到队伍
            if ("accepted".equals(action)) {
                // 检查该生是否已是队员
                if (teamDao.isMember(teamId, studentId)) {
                    return Result.error("该生已是队员");
                }
                
                // 检查该生是否有其他预占
                ThesisApplication existingReservation = applicationDao.selectByStudentIdAndStatus(studentId, 1);
                if (existingReservation != null) {
                    return Result.error("该生已有其他选题预占，无法加入");
                }
                
                // 检查队伍人数是否已满
                int memberCount = teamDao.countMembersByTeamId(teamId);
                if (memberCount >= team.getMaxMembers()) {
                    return Result.error("队伍人数已满");
                }
                
                // 添加为队员
                TeamMember member = new TeamMember();
                member.setTeamId(teamId);
                member.setStudentId(studentId);
                member.setRoleInTeam("组员");
                teamDao.insertMember(member);
                
                // 队伍有选题时，分配占位记录或新建预占
                if (team.getTopicId() != null) {
                    int assigned = applicationDao.assignPlaceholder(teamId, studentId, calcReserveUntil());
                    if (assigned <= 0) {
                        ThesisApplication newApp = new ThesisApplication();
                        newApp.setStudentId(studentId);
                        newApp.setTopicId(team.getTopicId());
                        newApp.setTeamId(teamId);
                        newApp.setApplyType("team");
                        newApp.setApplicationStatus(1);
                        newApp.setIsLocked(0);
                        newApp.setApplyTime(LocalDateTime.now());
                        newApp.setReserveUntil(calcReserveUntil());
                        applicationDao.insert(newApp);
                    }
                }
                
                logger.info("学生{}已加入队伍{}", studentId, teamId);
            }
            
            return Result.success("操作成功");
        } catch (Exception e) {
            logger.error("处理加入请求失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @Override
    public Result searchStudents(String keyword, int studentId) {
        try {
            // 查询当前学生的班级ID
            Integer classId = teamDao.selectStudentClassId(studentId);
            if (classId == null) {
                return Result.error("未找到班级信息");
            }
            
            // 搜索同班级学生
            List<Map<String, Object>> students = teamDao.searchStudents(keyword, classId);
            return Result.success(students);
        } catch (Exception e) {
            logger.error("搜索学生失败", e);
            return Result.error("搜索失败");
        }
    }

    @Override
    public Result updateMemberTask(int teamId, int studentId, String task) {
        try {
            // 验证学生是否在该队伍中
            if (!teamDao.isMember(teamId, studentId)) {
                return Result.error("您不是该队伍成员");
            }
            
            int rows = teamDao.updateMemberTask(teamId, studentId, task);
            return rows > 0 ? Result.success("职责更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            logger.error("更新职责失败", e);
            return Result.error("更新失败");
        }
    }
}
