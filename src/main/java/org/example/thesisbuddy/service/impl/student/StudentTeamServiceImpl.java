package org.example.thesisbuddy.service.impl.student;

import org.example.thesisbuddy.dao.TeamDao;
import org.example.thesisbuddy.dao.StudentDao;
import org.example.thesisbuddy.dao.TopicDao;
import org.example.thesisbuddy.dao.ApplicationDao;
import org.example.thesisbuddy.dto.student.TeamCreateDTO;
import org.example.thesisbuddy.dto.student.TeamInviteDTO;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.entity.TeamInfo;
import org.example.thesisbuddy.entity.TeamMember;
import org.example.thesisbuddy.entity.TopicLibrary;
import org.example.thesisbuddy.entity.ThesisApplication;
import org.example.thesisbuddy.service.student.StudentTeamService;
import org.example.thesisbuddy.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Override
    public Result createTeam(TeamCreateDTO createDTO) {
        if (studentDao.selectById(createDTO.getCaptainId()) == null) {
            return Result.error("队长不存在");
        }
        
        // 验证预计人数
        Integer expectedMembers = createDTO.getExpectedMembers();
        if (expectedMembers == null || expectedMembers < 2 || expectedMembers > 5) {
            return Result.error("预计人数必须在2-5人之间");
        }
        
        // 处理申请题目：如果提供了topicId，查询题目名称
        String applyTopic = null;
        Integer topicId = null;
        if (createDTO.getTopicId() != null) {
            TopicLibrary topic = topicDao.selectById(createDTO.getTopicId());
            if (topic == null) {
                return Result.error("选题不存在");
            }
            applyTopic = topic.getTopicName();
            topicId = topic.getTopicId();
        } else {
            return Result.error("请选择申请题目");
        }
        
        TeamInfo team = new TeamInfo();
        team.setCaptainId(createDTO.getCaptainId());
        team.setTeamName("队伍" + createDTO.getCaptainId()); // 自动生成队名
        team.setApplyTopic(applyTopic);
        team.setTopicId(topicId);
        team.setExpectedMembers(expectedMembers);
        team.setMaxMembers(expectedMembers); // max_members 与 expected_members 相同
        team.setStatus("forming");
        // reserve_until 在 SQL 中自动设置为当前时间 + 30分钟
        
        int rows = teamDao.insertTeam(team);
        if (rows <= 0) return Result.error("创建失败");
        
        // 添加队长为成员
        TeamMember captain = new TeamMember();
        captain.setTeamId(team.getTeamId());
        captain.setStudentId(createDTO.getCaptainId());
        captain.setRoleInTeam("队长");
        teamDao.insertMember(captain);
        
        // 添加初始队员（如果有）
        if (createDTO.getMemberIds() != null) {
            for (Integer studentId : createDTO.getMemberIds()) {
                TeamMember member = new TeamMember();
                member.setTeamId(team.getTeamId());
                member.setStudentId(studentId);
                member.setRoleInTeam("组员");
                teamDao.insertMember(member);
            }
        }
        
        // 创建预占记录：根据队伍最大人数进行预占（而非当前成员数）
        // 为队伍的最大人数创建预占记录，使用学生ID作为占位符
        // 队长的预占记录
        ThesisApplication captainApplication = new ThesisApplication();
        captainApplication.setStudentId(createDTO.getCaptainId());
        captainApplication.setTopicId(topicId);
        captainApplication.setTeamId(team.getTeamId());
        captainApplication.setApplyType("team");
        captainApplication.setApplicationStatus(1); // 1表示预占状态
        captainApplication.setIsLocked(0);
        captainApplication.setApplyTime(java.time.LocalDateTime.now());
        applicationDao.insert(captainApplication);
        
        // 为剩余的预占名额创建记录（使用队长ID作为占位，后续会被真实学生替换）
        int remainingSlots = expectedMembers - 1; // 减去队长已占的一个名额
        for (int i = 0; i < remainingSlots; i++) {
            ThesisApplication slotApplication = new ThesisApplication();
            slotApplication.setStudentId(createDTO.getCaptainId()); // 暂时使用队长ID
            slotApplication.setTopicId(topicId);
            slotApplication.setTeamId(team.getTeamId());
            slotApplication.setApplyType("team");
            slotApplication.setApplicationStatus(1); // 1表示预占状态
            slotApplication.setIsLocked(0);
            slotApplication.setApplyTime(java.time.LocalDateTime.now());
            applicationDao.insert(slotApplication);
        }
        
        return Result.success("队伍创建成功，名额已保留30分钟");
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
        
        // 如果有名额保留时间，计算剩余秒数
        if (team.getReserveUntil() != null) {
            long remainingSeconds = java.time.Duration.between(
                java.time.LocalDateTime.now(), 
                team.getReserveUntil()
            ).getSeconds();
            if (remainingSeconds > 0) {
                data.put("reserveUntil", remainingSeconds);
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
    public Result handleInvite(int inviteId, String action) {
        // 先查询邀请信息
        TeamInviteDTO targetInvite = teamDao.selectInviteById(inviteId);
        
        if (targetInvite == null) {
            return Result.error("邀请不存在");
        }
        
        // 更新邀请状态
        int rows = teamDao.updateInviteStatus(inviteId, action);
        if (rows <= 0) return Result.error("操作失败");
        
        // 如果接受邀请，将学生添加到队伍成员表
        if ("accepted".equals(action)) {
            // 检查是否已是队员
            if (teamDao.isMember(targetInvite.getTeamId(), targetInvite.getInviteeId())) {
                return Result.error("该学生已是队员");
            }
            
            // 检查队伍人数是否已满
            int memberCount = teamDao.countMembersByTeamId(targetInvite.getTeamId());
            TeamInfo team = teamDao.selectById(targetInvite.getTeamId());
            if (memberCount >= team.getMaxMembers()) {
                return Result.error("队伍人数已满");
            }
            
            // 添加为队员
            TeamMember member = new TeamMember();
            member.setTeamId(targetInvite.getTeamId());
            member.setStudentId(targetInvite.getInviteeId());
            member.setRoleInTeam("组员");
            teamDao.insertMember(member);
            
            // 更新预占记录：将一个预占名额的student_id更新为新成员ID
            // 查找该队伍的一个预占记录（使用队长ID作为占位的）
            List<ThesisApplication> reservedApps = applicationDao.selectReservedByTopic(team.getTopicId());
            for (ThesisApplication app : reservedApps) {
                if (app.getTeamId().equals(targetInvite.getTeamId()) && 
                    app.getStudentId().equals(team.getCaptainId()) &&
                    app.getApplicationStatus() == 1) {
                    // 更新这个预占记录的student_id为新成员
                    // 注意：这里需要更新数据库，但ApplicationDao没有update方法
                    // 所以我们将旧的预占记录释放，创建新的
                    AuditDTO releaseDTO = new AuditDTO();
                    releaseDTO.setApplicationId(app.getApplicationId());
                    releaseDTO.setApplicationStatus(0);
                    releaseDTO.setTeacherId(0);
                    releaseDTO.setRemark("成员加入，更新预占记录");
                    applicationDao.updateStatus(releaseDTO);
                    
                    // 创建新的预占记录
                    ThesisApplication newApp = new ThesisApplication();
                    newApp.setStudentId(targetInvite.getInviteeId());
                    newApp.setTopicId(team.getTopicId());
                    newApp.setTeamId(targetInvite.getTeamId());
                    newApp.setApplyType("team");
                    newApp.setApplicationStatus(1);
                    newApp.setIsLocked(0);
                    newApp.setApplyTime(java.time.LocalDateTime.now());
                    applicationDao.insert(newApp);
                    
                    break; // 只更新一个预占记录
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
        return rows > 0 ? Result.success("已退出队伍") : Result.error("退出失败");
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
        return rows > 0 ? Result.success("已踢出成员") : Result.error("踢出失败");
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
    public Result joinTeam(int studentId, int teamId, String reason) {
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
        
        // 直接加入队伍（简化流程，直接添加为组员）
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setStudentId(studentId);
        member.setRoleInTeam("组员");
        int rows = teamDao.insertMember(member);
        
        return rows > 0 ? Result.success("已成功加入队伍") : Result.error("加入失败");
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
                
                // 检查队伍人数是否已满
                int memberCount = teamDao.countMembersByTeamId(team.getTeamId());
                if (memberCount >= team.getMaxMembers()) {
                    continue;
                }
                
                Map<String, Object> teamInfo = new HashMap<>();
                teamInfo.put("teamId", team.getTeamId());
                teamInfo.put("applyTopic", team.getApplyTopic());
                teamInfo.put("maxMembers", team.getMaxMembers());
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
                // 检查队伍人数是否已满
                int memberCount = teamDao.countMembersByTeamId(teamId);
                if (memberCount >= team.getMaxMembers()) {
                    return Result.error("队伍人数已满");
                }
                
                // 检查是否已是队员
                if (teamDao.isMember(teamId, studentId)) {
                    return Result.error("该学生已是队员");
                }
                
                // 添加为队员
                TeamMember member = new TeamMember();
                member.setTeamId(teamId);
                member.setStudentId(studentId);
                member.setRoleInTeam("组员");
                teamDao.insertMember(member);
                
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
